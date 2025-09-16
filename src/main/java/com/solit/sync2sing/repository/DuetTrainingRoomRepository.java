package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.DuetTrainingRoom;
import com.solit.sync2sing.entity.TrainingSession;
import com.solit.sync2sing.global.type.DuetTrainingRoomStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DuetTrainingRoomRepository extends JpaRepository<DuetTrainingRoom, Long> {
    Optional<DuetTrainingRoom> findByHostTrainingSessionOrPartnerTrainingSession(TrainingSession hostSession, TrainingSession partnerSession);
    Optional<DuetTrainingRoom> findByHostTrainingSession(TrainingSession session);
    Optional<DuetTrainingRoom> findByPartnerTrainingSession(TrainingSession session);

    @EntityGraph(attributePaths = {
            "hostUserPart",
            "song",
            "song.albumCoverFile"
    })
    List<DuetTrainingRoom> findAllByStatusOrderByCreatedAtDesc(DuetTrainingRoomStatus status);
}
