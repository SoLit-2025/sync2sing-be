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
public class PostVocalAnalysisReportResponse extends GenerateVocalAnalysisReportResponse {

    private int prePitchScore;
    private int preBeatScore;
    private int prePronunciationScore;
    private int preBreathScore;

    private String feedbackTitle;
    private String feedbackContent;

    public static PostVocalAnalysisReportResponse toDTO(VocalAnalysisReport vocalAnalysisReport) {
        return PostVocalAnalysisReportResponse.builder()
                .prePitchScore(vocalAnalysisReport.getPreTrainingReport().getPitchScore())
                .preBeatScore(vocalAnalysisReport.getPreTrainingReport().getBeatScore())
                .prePronunciationScore(vocalAnalysisReport.getPreTrainingReport().getPronunciationScore())
                .preBreathScore(vocalAnalysisReport.getPreTrainingReport().getBreathScore())
                .reportId(vocalAnalysisReport.getId())
                .analysisType(vocalAnalysisReport.getReportType())
                .title(vocalAnalysisReport.getTitle())
                .song(GenerateVocalAnalysisReportResponse.SongDTO.toDTO(vocalAnalysisReport.getSong()))
                .pitchScore(vocalAnalysisReport.getPitchScore())
                .beatScore(vocalAnalysisReport.getBeatScore())
                .pronunciationScore(vocalAnalysisReport.getPronunciationScore())
                .breathScore(vocalAnalysisReport.getBreathScore())
                .overallReviewTitle(vocalAnalysisReport.getOverallReviewTitle())
                .overallReviewContent(vocalAnalysisReport.getOverallReviewContent())
                .feedbackTitle(vocalAnalysisReport.getFeedbackTitle())
                .feedbackContent(vocalAnalysisReport.getFeedbackContent())
                .createdAt(vocalAnalysisReport.getCreatedAt())
                .build();
    }
}
