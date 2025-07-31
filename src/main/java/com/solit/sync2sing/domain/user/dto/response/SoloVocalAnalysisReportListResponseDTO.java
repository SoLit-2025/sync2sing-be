package com.solit.sync2sing.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoloVocalAnalysisReportListResponseDTO {

    private int status;
    private String message;

    private List<SoloVocalAnalysisReportSummary> data;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SoloVocalAnalysisReportSummary {
        @JsonProperty("report_id")
        private Long reportId;
        private String title;
    }


}
