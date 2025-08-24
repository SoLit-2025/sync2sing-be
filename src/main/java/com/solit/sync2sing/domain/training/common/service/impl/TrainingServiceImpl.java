package com.solit.sync2sing.domain.training.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solit.sync2sing.domain.training.common.dto.*;
import com.solit.sync2sing.domain.training.common.service.TrainingService;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.ai.dto.AiVoiceAnalysisResponse;
import com.solit.sync2sing.global.ai.service.AiService;
import com.solit.sync2sing.global.chatgpt.dto.SoloPostResponse;
import com.solit.sync2sing.global.chatgpt.dto.SoloPreResponse;
import com.solit.sync2sing.global.chatgpt.service.ChatGPTService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.*;
import com.solit.sync2sing.global.util.MeasureTime;
import com.solit.sync2sing.global.util.S3Util;
import com.solit.sync2sing.global.transcription.service.transcriptionService;
import com.solit.sync2sing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
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
        List<TrainingDTO> vocalList  = pickTrainings(TrainingCategory.PRONUNCIATION,
                TrainingGrade.valueOf(request.getPronunciation()),
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
        List<TrainingSessionTraining> toSave = new ArrayList<>();
        toSave.addAll(buildSessionMappings(session, pitchList));
        toSave.addAll(buildSessionMappings(session, rhythmList));
        toSave.addAll(buildSessionMappings(session, vocalList));
        toSave.addAll(buildSessionMappings(session, breathList));
        trainingSessionTrainingRepository.saveAll(toSave);

        // 7) 세션 상태 TRAINING_IN_PROGRESS로 변경
        session.setStatus(SessionStatus.TRAINING_IN_PROGRESS);
        trainingSessionRepository.save(session);

        // 8) DTO 반환
        return CurriculumListResponse.builder()
                .pitch(pitchList)
                .rhythm(rhythmList)
                .pronunciation(vocalList)
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
                .sorted(Comparator.comparingLong(Training::getId))
                .map(TrainingDTO::toDTO)
                .collect(Collectors.toList());
    }


    private List<TrainingSessionTraining> buildSessionMappings(
            TrainingSession session,
            List<TrainingDTO> dtos
    ) {
        boolean firstFlag = true;
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
                    .isCurrentTraining(firstFlag)
                    .build();

            mappings.add(tst);

            firstFlag = false;
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
        TrainingMode mode = TrainingMode.valueOf(request.getTrainingMode());
        RecordingContext type = RecordingContext.valueOf(request.getAnalysisType());

        if (type.equals(RecordingContext.GUEST)) {
            return guestAnalysis(vocalFile, request);
        } else if (type.equals(RecordingContext.PRE)) {
            return preAnalysis(mode, vocalFile, request, userDetails);
        } else if (mode.equals(TrainingMode.SOLO) && type.equals(RecordingContext.POST)) {
            return soloPostAnalysis(vocalFile, request, userDetails);
        } else if (mode.equals(TrainingMode.DUET) && type.equals(RecordingContext.POST)) {
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

            String transcriptText = MeasureTime.run("transcriptFuture.get",
                    () -> {
                        try {
                            return transcriptFuture.get(60, TimeUnit.SECONDS);
                        } catch (ResponseStatusException rse) {
                            log.error("{} - {}", rse.getStatusCode(), rse.getReason(), rse);
                            throw rse;

                        } catch (Exception e) {
                            throw new ResponseStatusException(
                                    ResponseCode.TRANSCRIPTION_FAIL.getStatus(),
                                    ResponseCode.TRANSCRIPTION_FAIL.getMessage()
                            );
                        }
                    });
            AiVoiceAnalysisResponse aiResult = MeasureTime.run("aiFuture.get",
                    () -> {
                        try {
                            return aiFuture.get(60, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new ResponseStatusException(
                                    ResponseCode.AI_VOICE_ANALYSIS_FAIL.getStatus(),
                                    ResponseCode.AI_VOICE_ANALYSIS_FAIL.getMessage()
                            );
                        }
                    });

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

            String userPrompt =
                    "## 페르소나\n"
                    + "* 역할: 10년차 보컬 트레이닝 전문가\n"
                    + "* 대상: 음악 이론 기초 지식 미보유 초·중급 학습자\n"
                    + "* 톤: 다정·친절·간결·구체\n"
                    + "* 역량: 점수·태그 기반 진단, 즉시 실행 가능한 과제 제안\n"
                    + "\n"
                    + "## 레시피/절차 생성\n"
                    + "* 입력해석: 4개 항목별 점수(0~100) + 발성태그 상위3개(확률%) → 강점·보완 도출\n"
                    + "* 우선순위: 60점 미만 항목 → 60~80점 항목 → 80점 이상 항목 순\n"
                    + "* 작성순서: 제목(한줄) → 상태요약(2~4문장) → 가능한 원인(가설) → 실행 제안(구체적 행동 지시)\n"
                    + "\n"
                    + "## 템플릿\n"
                    + "* 출력형식: JSON 단일 객체, 키 추가·누락 금지, 값: 문자열\n"
                    + "* 스키마:\n"
                    + "{\n"
                    + "\"overallReviewTitle\": \"\",\n"
                    + "\"overallReviewContent\": \"\",\n"
                    + "\"causeContent\": \"\",\n"
                    + "\"proposalContent\": \"\"\n"
                    + "}\n"
                    + "* 글자수제한(공백포함):\n"
                    + "- overallReviewTitle: 18~25글자\n"
                    + "- overallReviewContent: 100~130글자\n"
                    + "- causeContent: 45~65글자\n"
                    + "- proposalContent: 50~70글자\n"
                    + "\n"
                    + "## 사실 점검 목록\n"
                    + "* 점수반영: 낮은 점수→보완, 높은 점수→유지\n"
                    + "* 태그활용: 상위태그·확률 기반, 확률 70%↑(주요특징), 40~69%(보조특징), 40%↓(언급최소화), 단정 금지·가능성 표현 적용\n"
                    + "\n"
                    + "## 반성/자기설명\n"
                    + "* 이해용이성: 초·중급 눈높이, 전문용어 풀어쓰기\n"
                    + "* 구체성: 연습법에 횟수, 시간 명시\n"
                    + "* 권장사항: '~해요'로 문장 종결\n"
                    + "* 금지사항: 이모지, 감탄사, 의성어, 과도한 격려, 발성 태그명과 확률 직접 제시, 4개 항목별 점수 직접 언급\n"
                    + "\n"
                    + "## 인지 검증자\n"
                    + "* JSON검증: 4개 키 존재, 모든 값은 문자열 타입\n"
                    + "* 길이검증: 각 필드별 글자수 제한 준수 (초과시 핵심 중심 간결화 재시도)\n"
                    + "* (초과시)축약규칙: 부사·수식어 → 중복 문구 → 예시 순 제거\n"
                    + "* 언어검증: 한국어만 사용\n"
                    + "\n"
                    + "## 컨텍스트 관리자\n"
                    + "* 필수입력: 음정·박자·발음·호흡 점수 + 발성태그TOP3(확률%)\n"
                    + "* 예외처리: 결측값은 해당 항목 언급 생략\n"
                    + "* 분석기준: 최저점수 항목을 중심으로 원인과 해결책 도출\n"
                    + "* 출력규칙: 반드시 JSON만 반환, 코드블록·주석·줄바꿈·백틱·설명문·마크다운 금지, 한국어 고정\n"
                    + "* 준수사항: 위 템플릿·제한·절차 절대 준수, 입력값 외 추론 금지"
                    + "\n"
                    + "## 입력\n" +
                    "음정 점수: " + request.getPitchAccuracy() + "\n" +
                    "박자 점수: " + request.getBeatAccuracy() + "\n" +
                    "발음 점수: " + pronunciationScore + "\n" +
                    "호흡 점수: " + breathScore + "\n" +
                    "발성 태그와 예측 확률: " + "\n" +
                    typeList.get(0) + " " + ratioList.get(0) + "\n" +
                    typeList.get(1) + " " + ratioList.get(1) + "\n" +
                    typeList.get(2) + " " + ratioList.get(2) + "\n"
                    ;

            String gptResponse = MeasureTime.run("askToGpt", () -> chatGPTService.askToGpt(userPrompt));

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
        } catch (ResponseStatusException rse) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            log.error("{} - {}", rse.getStatusCode(), rse.getReason(), rse);
            throw rse;

        } catch (Exception e) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            log.error("guestAnalysis 예상치 못한 예외 발생", e);
            throw new ResponseStatusException(
                    ResponseCode.INTERNAL_ERROR.getStatus(),
                    ResponseCode.INTERNAL_ERROR.getMessage()
            );
        }
    }

    private PreVocalAnalysisReportResponse preAnalysis(
            TrainingMode trainingMode,
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

            String transcriptText = MeasureTime.run("transcriptFuture.get",
                    () -> {
                        try {
                            return transcriptFuture.get(60, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new ResponseStatusException(
                                    ResponseCode.TRANSCRIPTION_FAIL.getStatus(),
                                    ResponseCode.TRANSCRIPTION_FAIL.getMessage()
                            );
                        }
                    });
            AiVoiceAnalysisResponse aiResult = MeasureTime.run("aiFuture.get",
                    () -> {
                        try {
                            return aiFuture.get(60, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new ResponseStatusException(
                                    ResponseCode.AI_VOICE_ANALYSIS_FAIL.getStatus(),
                                    ResponseCode.AI_VOICE_ANALYSIS_FAIL.getMessage()
                            );
                        }
                    });

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

            String userPrompt =
                    "## 페르소나\n"
                    + "* 역할: 10년차 보컬 트레이닝 전문가\n"
                    + "* 대상: 음악 이론 기초 지식 미보유 초·중급 학습자\n"
                    + "* 톤: 다정·친절·간결·구체\n"
                    + "* 역량: 점수·태그 기반 진단, 즉시 실행 가능한 과제 제안\n"
                    + "\n"
                    + "## 레시피/절차 생성\n"
                    + "* 입력해석: 4개 항목별 점수(0~100) + 발성태그 상위3개(확률%) → 강점·보완 도출\n"
                    + "* 우선순위: 60점 미만 항목 → 60~80점 항목 → 80점 이상 항목 순\n"
                    + "* 작성순서: 제목(한줄) → 상태요약(2~4문장) → 가능한 원인(가설) → 실행 제안(구체적 행동 지시)\n"
                    + "\n"
                    + "## 템플릿\n"
                    + "* 출력형식: JSON 단일 객체, 키 추가·누락 금지, 값: 문자열\n"
                    + "* 스키마:\n"
                    + "{\n"
                    + "\"overallReviewTitle\": \"\",\n"
                    + "\"overallReviewContent\": \"\",\n"
                    + "\"causeContent\": \"\",\n"
                    + "\"proposalContent\": \"\"\n"
                    + "}\n"
                    + "* 글자수제한(공백포함):\n"
                    + "- overallReviewTitle: 18~25글자\n"
                    + "- overallReviewContent: 100~130글자\n"
                    + "- causeContent: 45~65글자\n"
                    + "- proposalContent: 50~70글자\n"
                    + "\n"
                    + "## 사실 점검 목록\n"
                    + "* 점수반영: 낮은 점수→보완, 높은 점수→유지\n"
                    + "* 태그활용: 상위태그·확률 기반, 확률 70%↑(주요특징), 40~69%(보조특징), 40%↓(언급최소화), 단정 금지·가능성 표현 적용\n"
                    + "\n"
                    + "## 반성/자기설명\n"
                    + "* 이해용이성: 초·중급 눈높이, 전문용어 풀어쓰기\n"
                    + "* 구체성: 연습법에 횟수, 시간 명시\n"
                    + "* 권장사항: '~해요'로 문장 종결\n"
                    + "* 금지사항: 이모지, 감탄사, 의성어, 과도한 격려, 발성 태그명과 확률 직접 제시, 4개 항목별 점수 직접 언급\n"
                    + "\n"
                    + "## 인지 검증자\n"
                    + "* JSON검증: 4개 키 존재, 모든 값은 문자열 타입\n"
                    + "* 길이검증: 각 필드별 글자수 제한 준수 (초과시 핵심 중심 간결화 재시도)\n"
                    + "* (초과시)축약규칙: 부사·수식어 → 중복 문구 → 예시 순 제거\n"
                    + "* 언어검증: 한국어만 사용\n"
                    + "\n"
                    + "## 컨텍스트 관리자\n"
                    + "* 필수입력: 음정·박자·발음·호흡 점수 + 발성태그TOP3(확률%)\n"
                    + "* 예외처리: 결측값은 해당 항목 언급 생략\n"
                    + "* 분석기준: 최저점수 항목을 중심으로 원인과 해결책 도출\n"
                    + "* 출력규칙: 반드시 JSON만 반환, 코드블록·주석·줄바꿈·백틱·설명문·마크다운 금지, 한국어 고정\n"
                    + "* 준수사항: 위 템플릿·제한·절차 절대 준수, 입력값 외 추론 금지"
                    + "\n"
                    + "## 입력\n" +
                    "음정 점수: " + request.getPitchAccuracy() + "\n" +
                    "박자 점수: " + request.getBeatAccuracy() + "\n" +
                    "발음 점수: " + pronunciationScore + "\n" +
                    "호흡 점수: " + breathScore + "\n" +
                    "발성 태그와 예측 확률: " + "\n" +
                    typeList.get(0) + " " + ratioList.get(0) + "\n" +
                    typeList.get(1) + " " + ratioList.get(1) + "\n" +
                    typeList.get(2) + " " + ratioList.get(2) + "\n"
                    ;

            String gptResponse = MeasureTime.run("askToGpt", () -> chatGPTService.askToGpt(userPrompt));

            gptResponse = gptResponse.replaceAll("```json|```", "").trim();

            ObjectMapper objectMapper = new ObjectMapper();
            SoloPreResponse soloPreResponse = objectMapper.readValue(gptResponse, SoloPreResponse.class);

            String overallReviewTitle = soloPreResponse.getOverallReviewTitle();
            String overallReviewContent = soloPreResponse.getOverallReviewContent();
            String causeContent = soloPreResponse.getCauseContent();
            String proposalContent = soloPreResponse.getProposalContent();

            s3Util.deleteFileFromS3(recordingAudioS3Url);

            VocalAnalysisReport vocalAnalysisReport = VocalAnalysisReport.builder()
                    .user(userDetails.getUser())
                    .song(trsiningSong)
                    .title(vocalAnalysisReportTitle(trsiningSong.getTitle()))
                    .trainingMode(trainingMode)
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
        } catch (ResponseStatusException rse) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            log.error("{} - {}", rse.getStatusCode(), rse.getReason(), rse);
            throw rse;

        } catch (Exception e) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            log.error("preAnalysis 예상치 못한 예외 발생", e);
            throw new ResponseStatusException(
                    ResponseCode.INTERNAL_ERROR.getStatus(),
                    ResponseCode.INTERNAL_ERROR.getMessage()
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

            String transcriptText = MeasureTime.run("transcriptFuture.get",
                    () -> {
                        try {
                            return transcriptFuture.get(60, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new ResponseStatusException(
                                    ResponseCode.TRANSCRIPTION_FAIL.getStatus(),
                                    ResponseCode.TRANSCRIPTION_FAIL.getMessage()
                            );
                        }
                    });
            AiVoiceAnalysisResponse aiResult = MeasureTime.run("aiFuture.get",
                    () -> {
                        try {
                            return aiFuture.get(60, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new ResponseStatusException(
                                    ResponseCode.AI_VOICE_ANALYSIS_FAIL.getStatus(),
                                    ResponseCode.AI_VOICE_ANALYSIS_FAIL.getMessage()
                            );
                        }
                    });

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

            String userPrompt =
                    "## 페르소나\n"
                    + "* 역할: 10년차 보컬 트레이닝 전문가\n"
                    + "* 대상: 음악 이론 기초 지식 미보유 초·중급 학습자\n"
                    + "* 톤: 다정·친절·간결·구체\n"
                    + "* 역량: 점수·태그 기반 진단, 즉시 실행 가능한 과제 제안\n"
                    + "\n"
                    + "## 레시피/절차 생성\n"
                    + "* 입력해석: 4개 항목별 점수(0~100) + 발성태그 상위3개(확률%) → 강점·보완 도출\n"
                    + "* 우선순위: 60점 미만 항목 → 60~80점 항목 → 80점 이상 항목 순\n"
                    + "* 작성순서: 훈련 후 보컬에 대한 총평 제목(한 줄) → 상태요약(1~3문장) → 훈련 전후 차이에 대한 피드백 제목(한 줄) → 피드백 내용(구체적 행동 지시)\n"
                    + "\n"
                    + "## 템플릿\n"
                    + "* 출력형식: JSON 단일 객체, 키 추가·누락 금지, 값: 문자열\n"
                    + "* 스키마:\n"
                    + "{\n"
                    + "\"overallReviewTitle\": \"\",\n"
                    + "\"overallReviewContent\": \"\",\n"
                    + "\"feedbackTitle\": \"\",\n"
                    + "\"feedbackContent\": \"\"\n"
                    + "}\n"
                    + "* 글자수제한(공백포함):\n"
                    + "- overallReviewTitle: 18~25글자\n"
                    + "- overallReviewContent: 100~130글자\n"
                    + "- feedbackTitle: 18~25글자\n"
                    + "- feedbackContent: 100~130글자\n"
                    + "\n"
                    + "## 사실 점검 목록\n"
                    + "* 점수반영: 낮은 점수→보완, 높은 점수→유지\n"
                    + "* 태그활용: 상위태그·확률 기반, 확률 70%↑(주요특징), 40~69%(보조특징), 40%↓(언급최소화), 단정 금지·가능성 표현 적용\n"
                    + "\n"
                    + "## 반성/자기설명\n"
                    + "* 이해용이성: 초·중급 눈높이, 전문용어 풀어쓰기\n"
                    + "* 구체성: 연습법에 횟수, 시간 명시\n"
                    + "* 권장사항: '~해요'로 문장 종결\n"
                    + "* 금지사항: 이모지, 감탄사, 의성어, 과도한 격려, 발성 태그명과 확률 직접 제시, 4개 항목별 점수 직접 언급\n"
                    + "\n"
                    + "## 인지 검증자\n"
                    + "* JSON검증: 4개 키 존재, 모든 값은 문자열 타입\n"
                    + "* 길이검증: 각 필드별 글자수 제한 준수 (초과시 핵심 중심 간결화 재시도)\n"
                    + "* (초과시)축약규칙: 부사·수식어 → 중복 문구 → 예시 순 제거\n"
                    + "* 언어검증: 한국어만 사용\n"
                    + "\n"
                    + "## 컨텍스트 관리자\n"
                    + "* 필수입력: 음정·박자·발음·호흡 점수 + 발성태그TOP3(확률%)\n"
                    + "* 예외처리: 결측값은 해당 항목 언급 생략\n"
                    + "* 분석기준: 최저점수 항목을 중심으로 원인과 해결책 도출\n"
                    + "* 출력규칙: 반드시 JSON만 반환, 코드블록·주석·줄바꿈·백틱·설명문·마크다운 금지, 한국어 고정\n"
                    + "* 준수사항: 위 템플릿·제한·절차 절대 준수, 입력값 외 추론 금지"
                    + "\n"
                    + "## 입력\n" +
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
                    typeList.get(2) + " " + ratioList.get(2) + "\n"
                    ;

            String gptResponse = MeasureTime.run("askToGpt", () -> chatGPTService.askToGpt(userPrompt));

            gptResponse = gptResponse.replaceAll("```json|```", "").trim();

            ObjectMapper objectMapper = new ObjectMapper();
            SoloPostResponse soloPostResponse = objectMapper.readValue(gptResponse, SoloPostResponse.class);

            String overallReviewTitle = soloPostResponse.getOverallReviewTitle();
            String overallReviewContent = soloPostResponse.getOverallReviewContent();
            String feedbackTitle = soloPostResponse.getFeedbackTitle();
            String feedbackContent = soloPostResponse.getFeedbackContent();

            s3Util.deleteFileFromS3(recordingAudioS3Url);

            VocalAnalysisReport vocalAnalysisReport = VocalAnalysisReport.builder()
                    .user(userDetails.getUser())
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
        } catch (ResponseStatusException rse) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            log.error("{} - {}", rse.getStatusCode(), rse.getReason(), rse);
            throw rse;

        } catch (Exception e) {
            if (recordingAudioS3Url != null) s3Util.deleteFileFromS3(recordingAudioS3Url);

            log.error("soloPostAnalysis 예상치 못한 예외 발생", e);
            throw new ResponseStatusException(
                    ResponseCode.INTERNAL_ERROR.getStatus(),
                    ResponseCode.INTERNAL_ERROR.getMessage()
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
