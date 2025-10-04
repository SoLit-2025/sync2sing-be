package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.Song;
import com.solit.sync2sing.entity.VocalAnalysisReport;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.TrainingMode;
import org.springframework.data.jpa.repository.JpaRepository;
import com.solit.sync2sing.global.type.RecordingContext;

import java.util.List;

import java.util.Optional;

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

    // 가장 최신 PRE 리포트 하나
    Optional<VocalAnalysisReport> findTopBySongAndTrainingModeAndReportTypeOrderByCreatedAtDesc(
            Song song,
            TrainingMode trainingMode,
            RecordingContext reportType
    );
}
