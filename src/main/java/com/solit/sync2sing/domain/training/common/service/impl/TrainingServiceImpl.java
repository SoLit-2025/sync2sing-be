package com.solit.sync2sing.domain.training.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solit.sync2sing.domain.training.common.dto.*;
import com.solit.sync2sing.domain.training.common.service.TrainingService;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.ai.dto.AiVoiceAnalysisResponse;
import com.solit.sync2sing.global.ai.service.AiService;
import com.solit.sync2sing.global.chatgpt.dto.SoloPreResponse;
import com.solit.sync2sing.global.chatgpt.sevice.ChatGPTService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.*;
import com.solit.sync2sing.global.util.S3Util;
import com.solit.sync2sing.global.transcription.service.transcriptionService;
import com.solit.sync2sing.repository.*;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.stream.Collectors;

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
    private final VocalAnalysisReportRepository vocalAnalysisReportRepository;

    @Override
    public CurriculumListResponse generateTrainingCurriculum(
            CustomUserDetails userDetails,
            GenerateCurriculumRequest request
    ) {
        int trainingCountPerCategory = switch (request.getTrainingDays()) {
            case 3 -> 1;
            case 7 -> 2;
            case 14 -> 3;
            default -> throw new IllegalArgumentException("trainingDays는 3, 7, 14 중 하나여야 합니다.");
        };

        Long userId = userDetails.getId();

        Set<Long> trainedIds = userTrainingLogRepository.findTrainedTrainingIdsByUserId(userId);

        return CurriculumListResponse.builder()
                .pitch(pickTrainings(TrainingCategory.PITCH, TrainingGrade.valueOf(request.getPitch()), trainingCountPerCategory, trainedIds))
                .rhythm(pickTrainings(TrainingCategory.RHYTHM, TrainingGrade.valueOf(request.getRhythm()), trainingCountPerCategory, trainedIds))
                .vocalization(pickTrainings(TrainingCategory.VOCALIZATION, TrainingGrade.valueOf(request.getVocalization()), trainingCountPerCategory, trainedIds))
                .breath(pickTrainings(TrainingCategory.BREATH, TrainingGrade.valueOf(request.getBreath()), trainingCountPerCategory, trainedIds))
                .build();
    }

    private List<TrainingDTO> pickTrainings(
            TrainingCategory category,
            TrainingGrade grade,
            int count,
            Set<Long> trainedIds
    ) {
        List<Training> allTrainings = trainingRepository.findByCategory(category).stream()
                .filter(t -> t.getGrade().name().equals(grade.name()))
                .toList();

        List<Training> selected = allTrainings.stream()
                .filter(t -> !trainedIds.contains(t.getId()))
                .limit(count)
                .collect(Collectors.toList());

        if (selected.size() < count) {
            List<Training> fallback = allTrainings.stream()
                    .filter(t -> trainedIds.contains(t.getId()))
                    .filter(t -> selected.stream().noneMatch(u -> u.getId().equals(t.getId())))
                    .limit(count - selected.size())
                    .toList();

            selected.addAll(fallback);
        }

        selected.sort(Comparator.comparingLong(Training::getId));

        return selected.stream()
                .map(TrainingDTO::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    public TrainingDTO setTrainingProgress(
            CustomUserDetails userDetails,
            SetTrainingProgressRequest request,
            Long sessionId,
            Long trainingId) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("진행 중인 트레이닝 세션이 없습니다."));

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("해당 훈련이 존재하지 않습니다."));

        TrainingSessionTraining sessionTraining = trainingSessionTrainingRepository
                .findByTrainingSessionAndTraining(session, training)
                .orElseThrow(() -> new IllegalStateException("이 세션에 해당 훈련이 포함되어 있지 않습니다."));

        sessionTraining.setProgress(request.getProgress());

        return TrainingDTO.toDTO(training);
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
            List<TrainingSessionTraining> inProgressSessionTrainings =
                    trainingSessionTrainingRepository.findByTrainingSession(session);

            Map<TrainingCategory, TrainingSessionTraining> currentByCategory = inProgressSessionTrainings.stream()
                    .filter(training -> training.getProgress() < 100)
                    .collect(Collectors.groupingBy(
                            training -> training.getTraining().getCategory(),
                            Collectors.minBy(Comparator.comparing(training -> training.getTraining().getId()))
                    ))
                    .entrySet().stream()
                    .filter(e -> e.getValue().isPresent())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().get()
                    ));

            for (TrainingSessionTraining training : currentByCategory.values()) {
                Training t = training.getTraining();
                CurrentTrainingListDTO.CurrentTrainingDTO ct = CurrentTrainingListDTO.CurrentTrainingDTO.builder()
                        .id(t.getId())
                        .sessionId(session.getId())
                        .category(String.valueOf(t.getCategory()))
                        .title(t.getTitle())
                        .description(t.getDescription())
                        .grade(String.valueOf(t.getGrade()))
                        .trainingMinutes(t.getTrainingMinutes())
                        .progress(training.getProgress())
                        .isCurrentTraining(true)
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
            throw new IllegalArgumentException("잘못된 분석 요청");
        }
    }

    private PreVocalAnalysisReportResponse guestAnalysis(
            MultipartFile vocalFile,
            GenerateVocalAnalysisReportRequest request
    ) {
        Song guestSong = songRepository.findByTitle("Do-Re-Mi");

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
                    .title(vocalAnalysisReportTitle("Do-Re-Mi"))
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
        return null;
    }

    private GenerateVocalAnalysisReportResponse soloPostAnalysis(
            MultipartFile vocalFile,
            GenerateVocalAnalysisReportRequest request,
            CustomUserDetails userDetails
    ) {
        return null;
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
