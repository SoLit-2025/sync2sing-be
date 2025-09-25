package com.solit.sync2sing.domain.user.service;

import com.solit.sync2sing.domain.user.dto.response.DuetVocalAnalysisReportListResponseDTO;
import com.solit.sync2sing.global.security.CustomUserDetails;

public interface DuetVocalAnalysisReportListService {
    DuetVocalAnalysisReportListResponseDTO getDuetVocalAnalysisReportList(CustomUserDetails userDetails);

}
