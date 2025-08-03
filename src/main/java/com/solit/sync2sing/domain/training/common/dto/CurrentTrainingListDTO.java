package com.solit.sync2sing.domain.training.common.dto;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentTrainingListDTO {

    @Builder.Default
    private Map<String, CurrentTrainingDTO> solo = new HashMap<>();

    @Builder.Default
    private Map<String, CurrentTrainingDTO> duet = new HashMap<>();

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentTrainingDTO {
        private Long id;

        private Long sessionId;

        private String category;

        private String title;

        private String description;

        private String grade;

        private Integer trainingMinutes;

        private Integer progress;

        private boolean isCurrentTraining;
    }
}
