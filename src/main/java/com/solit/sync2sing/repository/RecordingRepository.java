package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.Recording;
import com.solit.sync2sing.entity.TrainingSession;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.RecordingFormat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordingRepository extends JpaRepository<Recording, Long> {
    Optional<Recording> findByTrainingSessionAndRecordingPhaseAndRecordingFormat(TrainingSession trainingSession, RecordingContext recordingPhase, RecordingFormat recordingFormat);
    List<Recording> findByTrainingSessionAndRecordingFormat(TrainingSession hostTrainingSession, RecordingFormat recordingFormat);
}
