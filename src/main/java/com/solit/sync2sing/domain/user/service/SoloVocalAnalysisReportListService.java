package com.solit.sync2sing.domain.user.service;

import com.solit.sync2sing.domain.user.dto.response.SoloVocalAnalysisReportListResponseDTO;
import com.solit.sync2sing.global.security.CustomUserDetails;


public interface SoloVocalAnalysisReportListService {
    SoloVocalAnalysisReportListResponseDTO getSoloVocalAnalysisReportList(CustomUserDetails userDetails);
}

