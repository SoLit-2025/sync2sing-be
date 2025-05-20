package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.Training;
import com.solit.sync2sing.entity.TrainingSession;
import com.solit.sync2sing.entity.TrainingSessionTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainingSessionTrainingRepository extends JpaRepository<TrainingSessionTraining, Long> {
    Optional<TrainingSessionTraining> findByTrainingSessionAndTraining(TrainingSession session, Training training);

    List<TrainingSessionTraining> findByTrainingSession(TrainingSession session);
}
