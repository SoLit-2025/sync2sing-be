package com.solit.sync2sing.domain.training.common.service.impl;

import com.solit.sync2sing.domain.training.common.dto.*;
import com.solit.sync2sing.domain.training.common.service.TrainingService;
import com.solit.sync2sing.entity.Training;
import com.solit.sync2sing.entity.TrainingSession;
import com.solit.sync2sing.entity.TrainingSessionTraining;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.SessionStatus;
import com.solit.sync2sing.global.type.TrainingCategory;
import com.solit.sync2sing.global.type.TrainingGrade;
import com.solit.sync2sing.repository.TrainingRepository;
import com.solit.sync2sing.repository.TrainingSessionRepository;
import com.solit.sync2sing.repository.TrainingSessionTrainingRepository;
import com.solit.sync2sing.repository.UserTrainingLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionTrainingRepository trainingSessionTrainingRepository;
    private final UserTrainingLogRepository userTrainingLogRepository;

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
    public VocalAnalysisReportDTO generateVocalAnalysisReport(
            String recordingFileUrl,
            GenerateVocalAnalysisReportRequest request) {
        return null;
    }

}
