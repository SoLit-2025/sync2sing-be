package com.solit.sync2sing.domain.training.common.dto;

import com.solit.sync2sing.entity.Training;
import com.solit.sync2sing.global.type.TrainingCategory;
import com.solit.sync2sing.global.type.TrainingGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDTO {
    private Long id;
    private TrainingCategory category;
    private String title;
    private TrainingGrade grade;
    private String description;
    private Integer trainingMinutes;

    public static TrainingDTO toDTO(Training training) {
        return TrainingDTO.builder()
            .id(training.getId())
            .category(TrainingCategory.valueOf(training.getCategory()))
            .title(training.getTitle())
            .grade(TrainingGrade.valueOf(training.getGrade()))
            .description(training.getDescription())
            .trainingMinutes(training.getTrainingMinutes())
            .build();
    }
}
