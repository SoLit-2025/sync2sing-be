package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.Recording;
import com.solit.sync2sing.entity.TrainingSession;
import com.solit.sync2sing.global.type.RecordingContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordingRepository extends JpaRepository<Recording, Long> {
    Optional<Recording> findByTrainingSessionAndRecordingPhase(TrainingSession trainingSession, RecordingContext recordingPhase);
}
