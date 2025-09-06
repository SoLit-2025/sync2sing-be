package com.solit.sync2sing.domain.training.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCurriculumRequest {

    @NotNull
    private String trainingMode;

    @NotNull
    private String pitch;

    @NotNull
    private String rhythm;

    @NotNull
    private String pronunciation;

    private int trainingDays;

}
