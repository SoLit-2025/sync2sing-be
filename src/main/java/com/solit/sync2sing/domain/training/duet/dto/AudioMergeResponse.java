package com.solit.sync2sing.domain.training.duet.dto;

import com.solit.sync2sing.domain.training.common.dto.GenerateVocalAnalysisReportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioMergeResponse {

    private Long roomId;

    private String recordingPhase;

    private String mergedAudioUrl;

    private GenerateVocalAnalysisReportResponse vocalAnalysisReportResponse;

}
