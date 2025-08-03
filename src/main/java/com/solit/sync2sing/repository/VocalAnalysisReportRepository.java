package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.Song;
import com.solit.sync2sing.entity.VocalAnalysisReport;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.TrainingMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VocalAnalysisReportRepository extends JpaRepository<VocalAnalysisReport, Long> {
    // 가장 최신 PRE 리포트 하나
    Optional<VocalAnalysisReport> findTopBySongAndTrainingModeAndReportTypeOrderByCreatedAtDesc(
            Song song,
            TrainingMode trainingMode,
            RecordingContext reportType
    );
}
