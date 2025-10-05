package com.solit.sync2sing.domain.user.service.impl;


import com.solit.sync2sing.domain.user.dto.response.DuetVocalAnalysisReportListResponseDTO;
import com.solit.sync2sing.domain.user.service.DuetVocalAnalysisReportListService;
import com.solit.sync2sing.entity.VocalAnalysisReport;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.repository.VocalAnalysisReportRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DuetVocalAnalysisReportListServiceImpl implements DuetVocalAnalysisReportListService {

    private final VocalAnalysisReportRepository vocalAnalysisReportRepository;
    private static final Logger logger = LoggerFactory.getLogger(DuetVocalAnalysisReportListServiceImpl.class);

    @Override
    public DuetVocalAnalysisReportListResponseDTO getDuetVocalAnalysisReportList(CustomUserDetails userDetails) {

        List<VocalAnalysisReport> duetReports = new ArrayList<>();

        if(userDetails != null) {
            Long userId = userDetails.getId();
            List<RecordingContext> duetReportTypes = List.of(RecordingContext.PRE,RecordingContext.POST);

            duetReports = vocalAnalysisReportRepository.findAllByUser_IdAndTrainingModeAndReportTypeInOrderByCreatedAtDesc(
                    userId,
                    TrainingMode.DUET,
                    duetReportTypes
            );
        }
        // 날짜 기준 최신순 정렬
        duetReports.sort((r1, r2) -> {
            LocalDate d1 = tryParseDateFromTitle(r1.getTitle());
            LocalDate d2 = tryParseDateFromTitle(r2.getTitle());
            if (d1 == null) d1 = LocalDate.MIN;
            if (d2 == null) d2 = LocalDate.MIN;
            return d2.compareTo(d1);
        });

        List<DuetVocalAnalysisReportListResponseDTO.DuetVocalAnalysisReportSummary> mergedList = duetReports.stream()
                .map(report -> DuetVocalAnalysisReportListResponseDTO.DuetVocalAnalysisReportSummary.builder()
                        .reportId(report.getId())
                        .title(report.getTitle())
                        .build())
                .collect(Collectors.toList());

        return DuetVocalAnalysisReportListResponseDTO.builder()
                .data(mergedList)
                .build();
    }
    private LocalDate tryParseDateFromTitle(String title) {
        try {
            if (title != null && title.length() >= 10) {
                String datePart = title.substring(0, 10);
                return LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE);
            }
        } catch (Exception e) {
            logger.warn("날짜 파싱 실패 - title: '{}', 오류: {}, 코드: {}",
                    title, e.getMessage(), ResponseCode.BAD_REQUEST.getMessage());
        }
        return null;
    }
}
