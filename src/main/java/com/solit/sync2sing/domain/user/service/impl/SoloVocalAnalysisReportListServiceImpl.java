package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.response.SoloVocalAnalysisReportListResponseDTO;
import com.solit.sync2sing.domain.user.service.SoloVocalAnalysisReportListService;
import com.solit.sync2sing.entity.VocalAnalysisReport;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.repository.VocalAnalysisReportRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoloVocalAnalysisReportListServiceImpl implements SoloVocalAnalysisReportListService {

    private final VocalAnalysisReportRepository vocalAnalysisReportRepository;

    private static final Logger logger = LoggerFactory.getLogger(SoloVocalAnalysisReportListServiceImpl.class);

    @Override
    public SoloVocalAnalysisReportListResponseDTO getSoloAndGuestVocalAnalysisReportList(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return SoloVocalAnalysisReportListResponseDTO.builder()
                    .status(ResponseCode.UNAUTHORIZED.getStatus().value())
                    .message(ResponseCode.UNAUTHORIZED.getMessage())
                    .data(List.of())
                    .build();
        }

        Long userId = userDetails.getId();

        // 1) SOLO 리포트 조회 (PRE, POST)
        List<RecordingContext> soloReportTypes = List.of(RecordingContext.PRE, RecordingContext.POST);
        List<VocalAnalysisReport> soloReports = vocalAnalysisReportRepository.findAllByUser_IdAndTrainingModeAndReportTypeInOrderByCreatedAtDesc(
                userId,
                TrainingMode.SOLO,
                soloReportTypes
        );

        // 2) GUEST 리포트 조회 (GUEST)
        List<VocalAnalysisReport> guestReports = vocalAnalysisReportRepository.findAllByTrainingModeAndReportTypeOrderByCreatedAtDesc(
                TrainingMode.GUEST,
                RecordingContext.GUEST
        );

        List<VocalAnalysisReport> combinedReports = new ArrayList<>();
        combinedReports.addAll(soloReports);
        combinedReports.addAll(guestReports);

        // 3) title에서 날짜 추출, 내림차순 정렬
        combinedReports.sort((r1, r2) -> {
            LocalDate d1 = tryParseDateFromTitle(r1.getTitle());
            LocalDate d2 = tryParseDateFromTitle(r2.getTitle());
            if (d1 == null) d1 = LocalDate.MIN;
            if (d2 == null) d2 = LocalDate.MIN;
            return d2.compareTo(d1);
        });

        List<SoloVocalAnalysisReportListResponseDTO.SoloVocalAnalysisReportSummary> mergedList = combinedReports.stream()
                .map(report -> SoloVocalAnalysisReportListResponseDTO.SoloVocalAnalysisReportSummary.builder()
                        .reportId(report.getId())
                        .title(report.getTitle())
                        .build())
                .collect(Collectors.toList());

        return SoloVocalAnalysisReportListResponseDTO.builder()
                .status(ResponseCode.SOLO_VOCAL_ANALYSIS_REPORT_LIST_FETCHED.getStatus().value())
                .message(ResponseCode.SOLO_VOCAL_ANALYSIS_REPORT_LIST_FETCHED.getMessage())
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
