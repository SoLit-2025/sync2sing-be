package com.solit.sync2sing.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuetVocalAnalysisReportListResponseDTO {

    @JsonProperty("duet_report_list")
    private List<DuetVocalAnalysisReportSummary> data;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuetVocalAnalysisReportSummary {
        @JsonProperty("report_id")
        private Long reportId;
        private String title;
    }


}
