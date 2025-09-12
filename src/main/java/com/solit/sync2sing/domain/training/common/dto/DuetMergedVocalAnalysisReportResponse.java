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
public class DuetMergedVocalAnalysisReportResponse extends GenerateVocalAnalysisReportResponse {

    private String feedbackTitle;
    private String feedbackContent;

    public static DuetMergedVocalAnalysisReportResponse toDTO(VocalAnalysisReport vocalAnalysisReport) {
        return DuetMergedVocalAnalysisReportResponse.builder()
                .reportId(vocalAnalysisReport.getId())
                .analysisType(vocalAnalysisReport.getReportType())
                .title(vocalAnalysisReport.getTitle())
                .song(GenerateVocalAnalysisReportResponse.SongDTO.toDTO(vocalAnalysisReport.getSong()))
                .pitchScore(vocalAnalysisReport.getPitchScore())
                .beatScore(vocalAnalysisReport.getBeatScore())
                .pronunciationScore(vocalAnalysisReport.getPronunciationScore())
                .overallReviewTitle(vocalAnalysisReport.getOverallReviewTitle())
                .overallReviewContent(vocalAnalysisReport.getOverallReviewContent())
                .feedbackTitle(vocalAnalysisReport.getFeedbackTitle())
                .feedbackContent(vocalAnalysisReport.getFeedbackContent())
                .createdAt(vocalAnalysisReport.getCreatedAt())
                .build();
    }
}
