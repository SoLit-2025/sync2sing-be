package com.solit.sync2sing.domain.user.service;

import com.solit.sync2sing.domain.user.dto.response.VocalAnalysisReportDetailResponseDTO;

public interface VocalAnalysisReportDetailService {

    VocalAnalysisReportDetailResponseDTO getVocalAnalysisReportDetail(Long reportId);

}
