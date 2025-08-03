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
public class GenerateVocalAnalysisReportRequest {

    @NotNull
    private String trainingMode;

    @NotNull
    private String analysisType;

    @NotNull
    private int pitchAccuracy;

    @NotNull
    private int beatAccuracy;

}
