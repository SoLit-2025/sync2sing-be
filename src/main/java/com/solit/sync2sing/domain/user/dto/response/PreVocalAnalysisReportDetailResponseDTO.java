package com.solit.sync2sing.domain.user.dto.response;


import com.solit.sync2sing.entity.VocalAnalysisReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PreVocalAnalysisReportDetailResponseDTO extends VocalAnalysisReportDetailResponseDTO {

    private String causeContent;
    private String proposalContent;


    public static PreVocalAnalysisReportDetailResponseDTO toDTO(VocalAnalysisReport report) {
        return PreVocalAnalysisReportDetailResponseDTO.builder()
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
                .causeContent(report.getCauseContent())
                .proposalContent(report.getProposalContent())
                .build();
    }

}
