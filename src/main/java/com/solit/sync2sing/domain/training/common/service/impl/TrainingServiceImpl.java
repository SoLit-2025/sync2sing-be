package com.solit.sync2sing.domain.training.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solit.sync2sing.domain.training.common.dto.*;
import com.solit.sync2sing.domain.training.common.service.TrainingService;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.ai.dto.AiVoiceAnalysisResponse;
import com.solit.sync2sing.global.ai.service.AiService;
import com.solit.sync2sing.global.chatgpt.dto.SoloPostResponse;
import com.solit.sync2sing.global.chatgpt.dto.SoloPreResponse;
import com.solit.sync2sing.global.chatgpt.sevice.ChatGPTService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.*;
import com.solit.sync2sing.global.util.S3Util;
import com.solit.sync2sing.global.transcription.service.transcriptionService;
import com.solit.sync2sing.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
class TrainingServiceImpl implements TrainingService {

    private final S3Util s3Util;

    private final transcriptionService transcriptionService;
    private final AiService aiService;
    private final ChatGPTService chatGPTService;

    private final TrainingRepository trainingRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionTrainingRepository trainingSessionTrainingRepository;
    private final UserTrainingLogRepository userTrainingLogRepository;
    private final SongRepository songRepository;
    private final LyricslineRepository lyricslineRepository;
    private final VocalAnalysisReportRepository vocalAnalysisReportRepository;

    @Override
    public CurriculumListResponse generateTrainingCurriculum(
            CustomUserDetails userDetails,
            GenerateCurriculumRequest request
    ) {
        // 1) days → count
        int trainingCountPerCategory = switch (request.getTrainingDays()) {
            case 3 -> 1;
            case 7 -> 2;
            case 14 -> 3;
            default -> throw new ResponseStatusException(
                    ResponseCode.INVALID_CURRICULUM_DAYS.getStatus(),
                    ResponseCode.INVALID_CURRICULUM_DAYS.getMessage()
            );
        };

        // 2) 사용자 세션 조회
        TrainingSession session = trainingSessionRepository.findByUser(userDetails.getUser()).stream()
                .filter(s -> s.getTrainingMode().name().equals(request.getTrainingMode()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getStatus(),
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getMessage()
                ));

        // 3) 기존 UserTrainingLog 전체 조회 → Map<trainingId, UserTrainingLog>
        List<UserTrainingLog> existingLogs =
                userTrainingLogRepository.findByUser(userDetails.getUser());
        Map<Long, UserTrainingLog> logMap = existingLogs.stream()
                .collect(Collectors.toMap(
                        log -> log.getTraining().getId(),
                        log -> log
                ));

        // 4) 각 카테고리별 추천 리스트
        List<TrainingDTO> pitchList  = pickTrainings(TrainingCategory.PITCH,
                TrainingGrade.valueOf(request.getPitch()),
                trainingCountPerCategory, logMap);
        List<TrainingDTO> rhythmList = pickTrainings(TrainingCategory.RHYTHM,
                TrainingGrade.valueOf(request.getRhythm()),
                trainingCountPerCategory, logMap);
        List<TrainingDTO> vocalList  = pickTrainings(TrainingCategory.VOCALIZATION,
                TrainingGrade.valueOf(request.getVocalization()),
                trainingCountPerCategory, logMap);
        List<TrainingDTO> breathList = pickTrainings(TrainingCategory.BREATH,
                TrainingGrade.valueOf(request.getBreath()),
                trainingCountPerCategory, logMap);

        // 5) UserTrainingLog 업데이트 및 저장
        List<UserTrainingLog> logsToSave = new ArrayList<>();
        Stream.of(pitchList, rhythmList, vocalList, breathList)
                .flatMap(List::stream)
                .forEach(dto -> {
                    UserTrainingLog log = logMap.get(dto.getId());
                    if (log != null) {
                        // 이미 있으면 +1
                        log.setTrainingCount(log.getTrainingCount() + 1);
                    } else {
                        // 새로 추천된 훈련은 count=1
                        log = UserTrainingLog.builder()
                                .user(userDetails.getUser())
                                .training(
                                        trainingRepository.findById(dto.getId())
                                                .orElseThrow(() -> new ResponseStatusException(
                                                        ResponseCode.TRAINING_NOT_FOUND.getStatus(),
                                                        ResponseCode.TRAINING_NOT_FOUND.getMessage()
                                                ))
                                )
                                .trainingCount(1)
                                .build();
                    }
                    logsToSave.add(log);
                });
        userTrainingLogRepository.saveAll(logsToSave);

        // 6) SessionTraining 매핑 저장
        AtomicBoolean first = new AtomicBoolean(true);
        List<TrainingSessionTraining> toSave = new ArrayList<>();
        toSave.addAll(buildSessionMappings(session, pitchList,  first));
        first.set(true);
        toSave.addAll(buildSessionMappings(session, rhythmList, first));
        first.set(true);
        toSave.addAll(buildSessionMappings(session, vocalList,  first));
        first.set(true);
        toSave.addAll(buildSessionMappings(session, breathList, first));
        trainingSessionTrainingRepository.saveAll(toSave);

        // 7) 세션 상태 TRAINING_IN_PROGRESS로 변경
        session.setStatus(SessionStatus.TRAINING_IN_PROGRESS);
        trainingSessionRepository.save(session);

        // 8) DTO 반환
        return CurriculumListResponse.builder()
                .pitch(pitchList)
                .rhythm(rhythmList)
                .vocalization(vocalList)
                .breath(breathList)
                .build();
    }


    private List<TrainingDTO> pickTrainings(
            TrainingCategory category,
            TrainingGrade grade,
            int count,
            Map<Long, UserTrainingLog> logMap
    ) {
        // 1) 해당 카테고리+등급 전체 훈련 조회
        List<Training> all = trainingRepository.findByCategory(category).stream()
                .filter(t -> t.getGrade() == grade)
                .collect(Collectors.toList());

        // 2) 훈련별 과거 수행 횟수 가져오기(없으면 0) 후 오름차순 정렬
        List<Training> sorted = all.stream()
                .sorted(Comparator.comparingInt(t -> {
                    UserTrainingLog log = logMap.get(t.getId());
                    return (log != null) ? log.getTrainingCount() : 0;
                }))
                .collect(Collectors.toList());

        // 3) 상위 count개 추천
        return sorted.stream()
                .limit(count)
                .map(TrainingDTO::toDTO)
                .collect(Collectors.toList());
    }


    private List<TrainingSessionTraining> buildSessionMappings(
            TrainingSession session,
            List<TrainingDTO> dtos,
            AtomicBoolean firstFlag
    ) {
        List<TrainingSessionTraining> mappings = new ArrayList<>();
        for (TrainingDTO dto : dtos) {
            Training training = trainingRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            ResponseCode.TRAINING_NOT_FOUND.getStatus(),
                            ResponseCode.TRAINING_NOT_FOUND.getMessage()
                    ));

            TrainingSessionTraining tst = TrainingSessionTraining.builder()
                    .trainingSession(session)
                    .training(training)
                    .progress(0)
                    .isCurrentTraining(firstFlag.getAndSet(false))
                    .build();

            mappings.add(tst);
        }
        return mappings;
    }


    @Override
    public SetTrainingProgressResponse setTrainingProgress(
            CustomUserDetails userDetails,
            SetTrainingProgressRequest request,
            Long sessionId,
            Long trainingId) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getStatus(),
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getMessage()
                ));

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.TRAINING_NOT_FOUND.getStatus(),
                        ResponseCode.TRAINING_NOT_FOUND.getMessage()
                ));

        TrainingSessionTraining sessionTraining = trainingSessionTrainingRepository
                .findByTrainingSessionAndTraining(session, training)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.INVALID_TRAINING_ID.getStatus(),
                        ResponseCode.INVALID_TRAINING_ID.getMessage()
                ));

        sessionTraining.setProgress(request.getProgress());
        trainingSessionTrainingRepository.save(sessionTraining);

        if (sessionTraining.getProgress() == 100) {
            sessionTraining.setCurrentTraining(false);
            trainingSessionTrainingRepository.save(sessionTraining);

            // 다음 순서 훈련 current=true 로 설정
            TrainingCategory category = sessionTraining.getTraining().getCategory();
            Long currId = sessionTraining.getTraining().getId();

            Optional<TrainingSessionTraining> nextOpt = trainingSessionTrainingRepository
                    .findByTrainingSession(session).stream()
                    .filter(tst -> tst.getTraining().getCategory() == category)
                    .filter(tst -> tst.getTraining().getId() > currId)
                    .min(Comparator.comparing(tst -> tst.getTraining().getId()));

            nextOpt.ifPresent(next -> {
                next.setCurrentTraining(true);
                trainingSessionTrainingRepository.save(next);
            });
        }

        // 모든 훈련의 progress가 100%인지 확인
        boolean allDone = trainingSessionTrainingRepository
                .findByTrainingSession(session).stream()
                .allMatch(tst -> tst.getProgress() != null && tst.getProgress() >= 100);

        // 모두 완료되었으면 세션 상태를 AFTER_TRAINING으로 변경
        if (allDone && session.getStatus() != SessionStatus.AFTER_TRAINING) {
            session.setStatus(SessionStatus.AFTER_TRAINING);
            trainingSessionRepository.save(session);
        }

        return SetTrainingProgressResponse.toDTO(sessionTraining);
    }

    @Override
    public CurrentTrainingListDTO getCurrentTrainingList(
            CustomUserDetails userDetails
    ) {
        CurrentTrainingListDTO result = new CurrentTrainingListDTO();

        List<TrainingSession> inProgressSessions =
                trainingSessionRepository.findByUserIdAndStatus(userDetails.getId(),
                        SessionStatus.TRAINING_IN_PROGRESS);

        if (inProgressSessions.isEmpty()) {
            return result;
        }

        for (TrainingSession session : inProgressSessions) {
            List<TrainingSessionTraining> currentTrainings =
                    trainingSessionTrainingRepository.findByTrainingSession(session).stream()
                    .filter(TrainingSessionTraining::isCurrentTraining)
                    .collect(Collectors.toList());

            for (TrainingSessionTraining training : currentTrainings) {
                Training t = training.getTraining();
                CurrentTrainingListDTO.CurrentTrainingDTO ct = CurrentTrainingListDTO.CurrentTrainingDTO.builder()
                        .id(t.getId())
                        .sessionId(session.getId())
                        .category(t.getCategory().name())
                        .title(t.getTitle())
                        .description(t.getDescription())
                        .grade(t.getGrade().name())
                        .trainingMinutes(t.getTrainingMinutes())
                        .progress(training.getProgress())
                        .isCurrentTraining(training.isCurrentTraining())
                        .build();

                if ("SOLO".equals(session.getTrainingMode().name())) {
                    result.getSolo().put(String.valueOf(t.getCategory()), ct);
                } else {
                    result.getDuet().put(String.valueOf(t.getCategory()), ct);
                }
            }
        }

        return result;
    }

    @Override
    public GenerateVocalAnalysisReportResponse generateVocalAnalysisReport(
            CustomUserDetails userDetails,
            MultipartFile vocalFile,
            GenerateVocalAnalysisReportRequest request
    ) {
        String mode = request.getTrainingMode();
        String type = request.getAnalysisType();

        if (type.equals("GUEST")) {
            return guestAnalysis(vocalFile, request);
        } else if (type.equals("PRE")) {
            return preAnalysis(vocalFile, request, userDetails);
        } else if (mode.equals("SOLO") && type.equals("POST")) {
            return soloPostAnalysis(vocalFile, request, userDetails);
        } else if (mode.equals("DUET") && type.equals("POST")) {
            return duetPostAnalysis(vocalFile, request, userDetails);
        } else {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getStatus(),
                    ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getMessage()
            );
        }
    }

    private PreVocalAnalysisReportResponse guestAnalysis(
            MultipartFile vocalFile,
            GenerateVocalAnalysisReportRequest request
    ) {
        Song guestSong = songRepository.findByTitle("Do-Re-Mi")
                .orElseThrow(() ->
                        new ResponseStatusException(
                                ResponseCode.SONG_NOT_FOUND.getStatus(),
                                ResponseCode.SONG_NOT_FOUND.getMessage()
                        )
                );

        String recordingAudioS3Url = null;

        try {
            recordingAudioS3Url = s3Util.saveRecordingAudioToS3(vocalFile);
            String jobName = "transcripts_job-" + UUID.randomUUID();

            CompletableFuture<String> transcriptFuture = transcriptionService.transcribeAndGetText(jobName, recordingAudioS3Url);
            CompletableFuture<AiVoiceAnalysisResponse> aiFuture = aiService.analyzeWithAiServer(recordingAudioS3Url);

            String transcriptText = transcriptFuture.get(60, TimeUnit.SECONDS);
            AiVoiceAnalysisResponse aiResult = aiFuture.get(60, TimeUnit.SECONDS);

            List<String> typeList = new ArrayList<>();
            List<String> ratioList = new ArrayList<>();

            for (int i = 0; i < aiResult.getData().getTop_voice_types().size(); i++) {
                String type = aiResult.getData().getTop_voice_types().get(i).getType();
                Double ratio = aiResult.getData().getTop_voice_types().get(i).getRatio();

                typeList.add(type);
                ratioList.add(String.format("%.3f", ratio));
            }

            String lyricText =
                    "Doe, a deer, a female deer " +
                    "Ray, a drop of golden sun";

            int pronunciationScore = calculateSimilarityScore(transcriptText, lyricText);

            // 호흡 평가
            int breathScore = 60;

            String userPrompt = "사용자의 음정, 박자, 발음, 호흡 점수와 사용자의 발성 유형 예측 결과 가장 확률이 높은 상위 3개 태그와 그 확률을 알려줄게.\n" +
                    "너는 총평 제목(overallReviewTitle), 총평 내용(overallReviewContent), 총평의 원인(causeContent), 추가적인 제안(proposalContent) 4가지를 알려줘.\n" +
                    "\n" +
                    "아래 네 가지 항목을 JSON 형식의 문자열로 답변해줘. 다른 말은 하지 말고 오직 JSON 형식의 문자열 응답만 줘.\n" +
                    "{\n" +
                    "  \"overallReviewTitle\": \"\",\n" +
                    "  \"overallReviewContent\": \"\",\n" +
                    "  \"causeContent\": \"\",\n" +
                    "  \"proposalContent\": \"\"\n" +
                    "}\n" +
                    "\n" +
                    "공백, 특수문자 포함 아래 규칙을 지켜줘.\n" +
                    "overall_review_title: 87Byte 이하\n" +
                    "overall_review_content: 389Byte 이하\n" +
                    "cause_content: 147Byte 이하\n" +
                    "proposal_content: 147Byte 이하\n" +
                    "\n" +
                    "음정 점수: " + request.getPitchAccuracy() + "\n" +
                    "박자 점수: " + request.getBeatAccuracy() + "\n" +
                    "발음 점수: " + pronunciationScore + "\n" +
                    "호흡 점수: " + breathScore + "\n" +
                    "발성 태그와 예측 확률: " + "\n" +
                    typeList.get(0) + " " + ratioList.get(0) + "\n" +
                    typeList.get(1) + " " + ratioList.get(1) + "\n" +
                    typeList.get(2) + " " + ratioList.get(2) + "\n" +
                    "다음은 너의 답변 예시를 알려줄게.\n" +
                    "\n" +
                    "{\n" +
                    "  \"overallReviewTitle\": \"호흡이 큰 장점이지만, 음정과 박자에 안정이 필요해요\",\n" +
                    "  \"overallReviewContent\": \"호흡 조절은 잘하고 계시지만, 음정과 박자가 불안정하여 노래의 화성 구조를 충분히 표현하지 못하고 있어요. 발성은 중간 정도로 괜찮지만, 정확한 음정과 리듬을 통해 전체적인 완성도를 높일 필요가 있습니다.\",\n" +
                    "  \"causeContent\": \"코드 변화를 정확히 인지하지 못해 화성 진행에 따른 음의 변화를 자연스럽게 표현하기 어려워요.\",\n" +
                    "  \"proposalContent\": \"주요 코드(C, F, G)의 느낌을 익히고, 단순한 발성 연습부터 시작해 듣기 훈련을 병행하세요.\"\n" +
                    "}";

            String gptResponse = chatGPTService.askToGpt(userPrompt);

            gptResponse = gptResponse.replaceAll("```json|```", "").trim();

            ObjectMapper objectMapper = new ObjectMapper();
            SoloPreResponse soloPreResponse = objectMapper.readValue(gptResponse, SoloPreResponse.class);

            String overallReviewTitle = soloPreResponse.getOverallReviewTitle();
            String overallReviewContent = soloPreResponse.getOverallReviewContent();
            String causeContent = soloPreResponse.getCauseContent();
            String proposalContent = soloPreResponse.getProposalContent();

            s3Util.deleteFileFromS3(recordingAudioS3Url);

            VocalAnalysisReport vocalAnalysisReport = VocalAnalysisReport.builder()
                    .song(guestSong)
                    .title(vocalAnalysisReportTitle(guestSong.getTitle()))
                    .trainingMode(TrainingMode.SOLO)
                    .reportType(RecordingContext.GUEST)
                    .pitchScore(request.getPitchAccuracy())
                    .beatScore(request.getBeatAccuracy())
                    .pronunciationScore(pronunciationScore)
                    .breathScore(breathScore)
                    .overallReviewTitle(overallReviewTitle)
                    .overallReviewContent(overallReviewContent)
                    .causeContent(causeContent)
                    .proposalContent(proposalContent)
                    .build();

            vocalAnalysisReportRepository.save(vocalAnalysisReport);

            return PreVocalAnalysisReportResponse.toDTO(vocalAnalysisReport);
        } catch (Exception e) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getStatus(),
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getMessage()
            );
        }
    }

    private PreVocalAnalysisReportResponse preAnalysis(
            MultipartFile vocalFile,
            GenerateVocalAnalysisReportRequest request,
            CustomUserDetails userDetails
    ) {
        // 사용자 세션 곡 조회
        TrainingSession session = trainingSessionRepository.findByUser(userDetails.getUser()).stream()
                .filter(s -> s.getTrainingMode().name().equals(request.getTrainingMode()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getStatus(),
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getMessage()
                ));
        Song trsiningSong = session.getSong();

        String recordingAudioS3Url = null;

        try {
            recordingAudioS3Url = s3Util.saveRecordingAudioToS3(vocalFile);
            String jobName = "transcripts_job-" + UUID.randomUUID();

            CompletableFuture<String> transcriptFuture = transcriptionService.transcribeAndGetText(jobName, recordingAudioS3Url);
            CompletableFuture<AiVoiceAnalysisResponse> aiFuture = aiService.analyzeWithAiServer(recordingAudioS3Url);

            String transcriptText = transcriptFuture.get(60, TimeUnit.SECONDS);
            AiVoiceAnalysisResponse aiResult = aiFuture.get(60, TimeUnit.SECONDS);

            List<String> typeList = new ArrayList<>();
            List<String> ratioList = new ArrayList<>();

            for (int i = 0; i < aiResult.getData().getTop_voice_types().size(); i++) {
                String type = aiResult.getData().getTop_voice_types().get(i).getType();
                Double ratio = aiResult.getData().getTop_voice_types().get(i).getRatio();

                typeList.add(type);
                ratioList.add(String.format("%.3f", ratio));
            }

            // 전체 가사 조회
            List<Lyricsline> lines = lyricslineRepository.findBySongOrderByLineIndex(trsiningSong);
            String lyricText = "";
            lyricText += lines.stream()
                    .map(Lyricsline::getText) + " ";

            int pronunciationScore = calculateSimilarityScore(transcriptText, lyricText);

            // 호흡 평가
            int breathScore = 60;

            String userPrompt = "사용자의 음정, 박자, 발음, 호흡 점수와 사용자의 발성 유형 예측 결과 가장 확률이 높은 상위 3개 태그와 그 확률을 알려줄게.\n" +
                    "너는 총평 제목(overallReviewTitle), 총평 내용(overallReviewContent), 총평의 원인(causeContent), 추가적인 제안(proposalContent) 4가지를 알려줘.\n" +
                    "\n" +
                    "아래 네 가지 항목을 JSON 형식의 문자열로 답변해줘. 다른 말은 하지 말고 오직 JSON 형식의 문자열 응답만 줘.\n" +
                    "{\n" +
                    "  \"overallReviewTitle\": \"\",\n" +
                    "  \"overallReviewContent\": \"\",\n" +
                    "  \"causeContent\": \"\",\n" +
                    "  \"proposalContent\": \"\"\n" +
                    "}\n" +
                    "\n" +
                    "공백, 특수문자 포함 아래 규칙을 지켜줘.\n" +
                    "overall_review_title: 87Byte 이하\n" +
                    "overall_review_content: 389Byte 이하\n" +
                    "cause_content: 147Byte 이하\n" +
                    "proposal_content: 147Byte 이하\n" +
                    "\n" +
                    "음정 점수: " + request.getPitchAccuracy() + "\n" +
                    "박자 점수: " + request.getBeatAccuracy() + "\n" +
                    "발음 점수: " + pronunciationScore + "\n" +
                    "호흡 점수: " + breathScore + "\n" +
                    "발성 태그와 예측 확률: " + "\n" +
                    typeList.get(0) + " " + ratioList.get(0) + "\n" +
                    typeList.get(1) + " " + ratioList.get(1) + "\n" +
                    typeList.get(2) + " " + ratioList.get(2) + "\n" +
                    "다음은 너의 답변 예시를 알려줄게.\n" +
                    "\n" +
                    "{\n" +
                    "  \"overallReviewTitle\": \"호흡이 큰 장점이지만, 음정과 박자에 안정이 필요해요\",\n" +
                    "  \"overallReviewContent\": \"호흡 조절은 잘하고 계시지만, 음정과 박자가 불안정하여 노래의 화성 구조를 충분히 표현하지 못하고 있어요. 발성은 중간 정도로 괜찮지만, 정확한 음정과 리듬을 통해 전체적인 완성도를 높일 필요가 있습니다.\",\n" +
                    "  \"causeContent\": \"코드 변화를 정확히 인지하지 못해 화성 진행에 따른 음의 변화를 자연스럽게 표현하기 어려워요.\",\n" +
                    "  \"proposalContent\": \"주요 코드(C, F, G)의 느낌을 익히고, 단순한 발성 연습부터 시작해 듣기 훈련을 병행하세요.\"\n" +
                    "}";

            String gptResponse = chatGPTService.askToGpt(userPrompt);

            gptResponse = gptResponse.replaceAll("```json|```", "").trim();

            ObjectMapper objectMapper = new ObjectMapper();
            SoloPreResponse soloPreResponse = objectMapper.readValue(gptResponse, SoloPreResponse.class);

            String overallReviewTitle = soloPreResponse.getOverallReviewTitle();
            String overallReviewContent = soloPreResponse.getOverallReviewContent();
            String causeContent = soloPreResponse.getCauseContent();
            String proposalContent = soloPreResponse.getProposalContent();

            s3Util.deleteFileFromS3(recordingAudioS3Url);

            VocalAnalysisReport vocalAnalysisReport = VocalAnalysisReport.builder()
                    .song(trsiningSong)
                    .title(vocalAnalysisReportTitle(trsiningSong.getTitle()))
                    .trainingMode(TrainingMode.SOLO)
                    .reportType(RecordingContext.PRE)
                    .pitchScore(request.getPitchAccuracy())
                    .beatScore(request.getBeatAccuracy())
                    .pronunciationScore(pronunciationScore)
                    .breathScore(breathScore)
                    .overallReviewTitle(overallReviewTitle)
                    .overallReviewContent(overallReviewContent)
                    .causeContent(causeContent)
                    .proposalContent(proposalContent)
                    .build();

            vocalAnalysisReportRepository.save(vocalAnalysisReport);

            return PreVocalAnalysisReportResponse.toDTO(vocalAnalysisReport);
        } catch (Exception e) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getStatus(),
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getMessage()
            );
        }
    }

    private GenerateVocalAnalysisReportResponse soloPostAnalysis(
            MultipartFile vocalFile,
            GenerateVocalAnalysisReportRequest request,
            CustomUserDetails userDetails
    ) {
        // 사용자 세션 곡 조회
        TrainingSession session = trainingSessionRepository.findByUser(userDetails.getUser()).stream()
                .filter(s -> s.getTrainingMode().name().equals(request.getTrainingMode()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getStatus(),
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getMessage()
                ));
        Song trsiningSong = session.getSong();

        // 훈련 전 보컬분석리포트
        VocalAnalysisReport preReport = vocalAnalysisReportRepository
                .findTopBySongAndTrainingModeAndReportTypeOrderByCreatedAtDesc(
                        trsiningSong, TrainingMode.SOLO, RecordingContext.PRE
                )
                .orElse(null);

        String recordingAudioS3Url = null;

        try {
            recordingAudioS3Url = s3Util.saveRecordingAudioToS3(vocalFile);
            String jobName = "transcripts_job-" + UUID.randomUUID();

            CompletableFuture<String> transcriptFuture = transcriptionService.transcribeAndGetText(jobName, recordingAudioS3Url);
            CompletableFuture<AiVoiceAnalysisResponse> aiFuture = aiService.analyzeWithAiServer(recordingAudioS3Url);

            String transcriptText = transcriptFuture.get(60, TimeUnit.SECONDS);
            AiVoiceAnalysisResponse aiResult = aiFuture.get(60, TimeUnit.SECONDS);

            List<String> typeList = new ArrayList<>();
            List<String> ratioList = new ArrayList<>();

            for (int i = 0; i < aiResult.getData().getTop_voice_types().size(); i++) {
                String type = aiResult.getData().getTop_voice_types().get(i).getType();
                Double ratio = aiResult.getData().getTop_voice_types().get(i).getRatio();

                typeList.add(type);
                ratioList.add(String.format("%.3f", ratio));
            }

            // 전체 가사 조회
            List<Lyricsline> lines = lyricslineRepository.findBySongOrderByLineIndex(trsiningSong);
            String lyricText = "";
            lyricText += lines.stream()
                    .map(Lyricsline::getText) + " ";

            int pronunciationScore = calculateSimilarityScore(transcriptText, lyricText);

            // 호흡 평가
            int breathScore = 60;

            String userPrompt = "사용자의 보컬 훈련 전과 훈련 후의 각 음정, 박자, 발음, 호흡 점수와 훈련 후 사용자의 발성 유형 예측 결과 가장 확률이 높은 상위 3개 태그와 그 확률을 알려줄게.\n" +
                    "너는 훈련 후 보컬에 대한 총평 제목(overallReviewTitle), 총평 내용(overallReviewContent), 훈련 전후 차이에 대한 피드백 제목(feedbackTitle), 피드백 내용(feedbackContent) 4가지를 알려줘.\n" +
                    "\n" +
                    "아래 네 가지 항목을 JSON 형식의 문자열로 답변해줘. 다른 말은 하지 말고 오직 JSON 형식의 문자열 응답만 줘.\n" +
                    "{\n" +
                    "  \"overallReviewTitle\": \"\",\n" +
                    "  \"overallReviewContent\": \"\",\n" +
                    "  \"feedbackTitle\": \"\",\n" +
                    "  \"feedbackContent\": \"\"\n" +
                    "}\n" +
                    "\n" +
                    "공백, 특수문자 포함 아래 규칙을 지켜줘.\n" +
                    "overall_review_title: 87Byte 이하\n" +
                    "overall_review_content: 389Byte 이하\n" +
                    "feedback_title: 87Byte 이하\n" +
                    "feedback_content: 389Byte 이하\n" +
                    "\n" +
                    "훈련 전 음정 점수: " + preReport.getPitchScore() + "\n" +
                    "훈련 전 박자 점수: " + preReport.getBeatScore() + "\n" +
                    "훈련 전 발음 점수: " + preReport.getPronunciationScore() + "\n" +
                    "훈련 전 호흡 점수: " + preReport.getBreathScore() + "\n" +
                    "훈련 후 음정 점수: " + request.getPitchAccuracy() + "\n" +
                    "훈련 후 박자 점수: " + request.getBeatAccuracy() + "\n" +
                    "훈련 후 발음 점수: " + pronunciationScore + "\n" +
                    "훈련 후 호흡 점수: " + breathScore + "\n" +
                    "훈련 후 발성 태그와 예측 확률: " + "\n" +
                    typeList.get(0) + " " + ratioList.get(0) + "\n" +
                    typeList.get(1) + " " + ratioList.get(1) + "\n" +
                    typeList.get(2) + " " + ratioList.get(2) + "\n" +
                    "다음은 너의 답변 예시를 알려줄게.\n" +
                    "\n" +
                    "{\n" +
                    "  \"overallReviewTitle\": \"호흡이 큰 장점이지만, 음정과 박자에 안정이 필요해요\",\n" +
                    "  \"overallReviewContent\": \"호흡 조절은 잘하고 계시지만, 음정과 박자가 불안정하여 노래의 화성 구조를 충분히 표현하지 못하고 있어요. 발성은 중간 정도로 괜찮지만, 정확한 음정과 리듬을 통해 전체적인 완성도를 높일 필요가 있습니다.\",\n" +
                    "  \"feedbackTitle\": \"꾸준함의 힘, 눈에 띄는 성장\",\n" +
                    "  \"feedbackContent\": \"전반적인 퍼포먼스가 크게 향상되었습니다. 훈련 진행률 85%를 달성하였으며, 꾸준한 연습을 통해 더욱 발전할 수 있습니다.\"\n" +
                    "}";

            String gptResponse = chatGPTService.askToGpt(userPrompt);

            gptResponse = gptResponse.replaceAll("```json|```", "").trim();

            ObjectMapper objectMapper = new ObjectMapper();
            SoloPostResponse soloPostResponse = objectMapper.readValue(gptResponse, SoloPostResponse.class);

            String overallReviewTitle = soloPostResponse.getOverallReviewTitle();
            String overallReviewContent = soloPostResponse.getOverallReviewContent();
            String feedbackTitle = soloPostResponse.getFeedbackTitle();
            String feedbackContent = soloPostResponse.getFeedbackContent();

            s3Util.deleteFileFromS3(recordingAudioS3Url);

            VocalAnalysisReport vocalAnalysisReport = VocalAnalysisReport.builder()
                    .song(trsiningSong)
                    .title(vocalAnalysisReportTitle(trsiningSong.getTitle()))
                    .trainingMode(TrainingMode.SOLO)
                    .reportType(RecordingContext.POST)
                    .pitchScore(request.getPitchAccuracy())
                    .beatScore(request.getBeatAccuracy())
                    .pronunciationScore(pronunciationScore)
                    .breathScore(breathScore)
                    .overallReviewTitle(overallReviewTitle)
                    .overallReviewContent(overallReviewContent)
                    .feedbackTitle(feedbackTitle)
                    .feedbackContent(feedbackContent)
                    .preTrainingReport(preReport)
                    .build();

            vocalAnalysisReportRepository.save(vocalAnalysisReport);

            return PostVocalAnalysisReportResponse.toDTO(vocalAnalysisReport);
        } catch (Exception e) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getStatus(),
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getMessage()
            );
        }
    }

    private GenerateVocalAnalysisReportResponse duetPostAnalysis(
            MultipartFile vocalFile,
            GenerateVocalAnalysisReportRequest request,
            CustomUserDetails userDetails
    ) {
        return null;
    }

    private String vocalAnalysisReportTitle(String songTitle) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return date + " " + songTitle;
    }



    public int calculateSimilarityScore(String sttText, String reference) {
        LevenshteinDistance ld = new LevenshteinDistance();
        int distance = ld.apply(sttText, reference);
        int maxLen = Math.max(sttText.length(), reference.length());

        // 100점 만점 스케일로 변환
        return (int) ((1.0 - ((double) distance / maxLen)) * 100);
    }
}
