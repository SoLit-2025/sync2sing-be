package com.solit.sync2sing.domain.training.common.dto;

import com.solit.sync2sing.entity.TrainingSessionTraining;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetTrainingProgressResponse {

    private Long sessionId;
    private Long trainingId;
    private String category;
    private String title;
    private String grade;
    private String description;
    private Integer trainingMinutes;
    private Integer progress;
    private boolean isCurrentTraining;

    public static SetTrainingProgressResponse toDTO(TrainingSessionTraining sessionTraining) {
        return SetTrainingProgressResponse.builder()
                .sessionId(sessionTraining.getTrainingSession().getId())
                .trainingId(sessionTraining.getTraining().getId())
                .category(sessionTraining.getTraining().getCategory().name())
                .title(sessionTraining.getTraining().getTitle())
                .grade(sessionTraining.getTraining().getGrade().name())
                .description(sessionTraining.getTraining().getDescription())
                .trainingMinutes(sessionTraining.getTraining().getTrainingMinutes())
                .progress(sessionTraining.getProgress())
                .isCurrentTraining(sessionTraining.isCurrentTraining())
                .build();

    }
}
