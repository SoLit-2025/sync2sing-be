package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.VocalAnalysisReport;
import com.solit.sync2sing.global.type.TrainingMode;
import org.springframework.data.jpa.repository.JpaRepository;
import com.solit.sync2sing.global.type.RecordingContext;

import java.util.List;

public interface VocalAnalysisReportRepository extends JpaRepository<VocalAnalysisReport, Long> {

    List<VocalAnalysisReport> findAllByUser_IdAndTrainingModeAndReportTypeInOrderByCreatedAtDesc(
            Long userId,
            TrainingMode trainingMode,
            List<RecordingContext> reportTypes
    );

    // 비로그인 Guest
    List<VocalAnalysisReport> findAllByTrainingModeAndReportTypeOrderByCreatedAtDesc(
            TrainingMode trainingMode,
            RecordingContext reportType
    );

}
