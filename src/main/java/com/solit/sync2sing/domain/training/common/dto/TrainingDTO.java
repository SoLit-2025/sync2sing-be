package com.solit.sync2sing.domain.training.common.dto;

import com.solit.sync2sing.entity.Training;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDTO {
    private Long id;
    private String category;
    private String title;
    private String grade;
    private String description;
    private Integer trainingMinutes;
    private List<ImageDTO> imageList;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDTO {
        private int order;
        private String url;
    }

    public static TrainingDTO toDTO(Training training) {
        return TrainingDTO.builder()
            .id(training.getId())
            .category(String.valueOf(training.getCategory()))
            .title(training.getTitle())
            .grade(String.valueOf(training.getGrade()))
            .description(training.getDescription())
            .trainingMinutes(training.getTrainingMinutes())
            .build();
    }
}
