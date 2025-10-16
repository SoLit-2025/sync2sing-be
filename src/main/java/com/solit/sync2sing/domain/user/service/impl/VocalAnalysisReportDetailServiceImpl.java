package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.response.*;
import com.solit.sync2sing.domain.user.service.VocalAnalysisReportDetailService;
import com.solit.sync2sing.entity.VocalAnalysisReport;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.repository.VocalAnalysisReportRepository;
import com.solit.sync2sing.global.type.RecordingContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class VocalAnalysisReportDetailServiceImpl implements VocalAnalysisReportDetailService {

    private final VocalAnalysisReportRepository reportRepository;

    public VocalAnalysisReportDetailServiceImpl(VocalAnalysisReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public VocalAnalysisReportDetailResponseDTO getVocalAnalysisReportDetail(Long reportId) {
        VocalAnalysisReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.VOCAL_ANALYSIS_REPORT_NOT_FOUND.getStatus(),
                        ResponseCode.VOCAL_ANALYSIS_REPORT_NOT_FOUND.getMessage()
                ));

        // 분석 타입에 따라 DTO 변환
        RecordingContext type = report.getReportType();
        if (type == RecordingContext.GUEST || type == RecordingContext.PRE) {
            return PreVocalAnalysisReportDetailResponseDTO.toDTO(report);
        } else if (type == RecordingContext.POST) {
            return PostVocalAnalysisReportDetailResponseDTO.toDTO(report);
        } else {
            throw new IllegalArgumentException("Unknown report type: " + type);
        }
    }
}
