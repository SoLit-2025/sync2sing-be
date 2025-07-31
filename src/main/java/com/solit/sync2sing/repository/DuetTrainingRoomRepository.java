package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.DuetTrainingRoom;
import com.solit.sync2sing.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DuetTrainingRoomRepository extends JpaRepository<DuetTrainingRoom, Long> {
    Optional<DuetTrainingRoom> findByHostTrainingSessionOrPartnerTrainingSession(TrainingSession hostSession, TrainingSession partnerSession);
}
