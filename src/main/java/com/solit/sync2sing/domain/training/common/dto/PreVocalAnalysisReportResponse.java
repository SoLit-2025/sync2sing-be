package com.solit.sync2sing.domain.training.common.dto;

import com.solit.sync2sing.entity.VocalAnalysisReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PreVocalAnalysisReportResponse extends GenerateVocalAnalysisReportResponse {

    private String causeContent;
    private String proposalContent;

    public static PreVocalAnalysisReportResponse toDTO(VocalAnalysisReport vocalAnalysisReport) {
        return PreVocalAnalysisReportResponse.builder()
                .reportId(vocalAnalysisReport.getId())
                .analysisType(vocalAnalysisReport.getReportType())
                .title(vocalAnalysisReport.getTitle())
                .song(GenerateVocalAnalysisReportResponse.SongDTO.toDTO(vocalAnalysisReport.getSong()))
                .pitchScore(vocalAnalysisReport.getPitchScore())
                .beatScore(vocalAnalysisReport.getBeatScore())
                .pronunciationScore(vocalAnalysisReport.getPronunciationScore())
                .overallReviewTitle(vocalAnalysisReport.getOverallReviewTitle())
                .overallReviewContent(vocalAnalysisReport.getOverallReviewContent())
                .causeContent(vocalAnalysisReport.getCauseContent())
                .proposalContent(vocalAnalysisReport.getProposalContent())
                .createdAt(vocalAnalysisReport.getCreatedAt())
                .build();
    }
}
