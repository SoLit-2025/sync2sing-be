package com.solit.sync2sing.domain.user.dto.response;

import com.solit.sync2sing.entity.VocalAnalysisReport;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class PostVocalAnalysisReportDetailResponseDTO extends VocalAnalysisReportDetailResponseDTO{

    private int prePitchScore;
    private int preBeatScore;
    private int prePronunciationScore;

    private String feedbackTitle;
    private String feedbackContent;


    public static PostVocalAnalysisReportDetailResponseDTO toDTO(VocalAnalysisReport report) {
        return PostVocalAnalysisReportDetailResponseDTO.builder()
                .reportId(report.getId())
                .analysisType(report.getReportType())
                .title(report.getTitle())
                .song(VocalAnalysisReportDetailResponseDTO.SongDTO.toDTO(report.getSong()))
                .pitchScore(report.getPitchScore())
                .beatScore(report.getBeatScore())
                .pronunciationScore(report.getPronunciationScore())
                .overallReviewTitle(report.getOverallReviewTitle())
                .overallReviewContent(report.getOverallReviewContent())
                .createdAt(report.getCreatedAt())
                .prePitchScore(report.getPreTrainingReport().getPitchScore())
                .preBeatScore(report.getPreTrainingReport().getBeatScore())
                .prePronunciationScore(report.getPreTrainingReport().getPronunciationScore())
                .feedbackTitle(report.getFeedbackTitle())
                .feedbackContent(report.getFeedbackContent())
                .build();
    }

}
