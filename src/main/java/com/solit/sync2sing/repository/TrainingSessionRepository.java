package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.TrainingSession;
import com.solit.sync2sing.global.type.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByUserIdAndStatus(Long userId, SessionStatus sessionStatus);
    List<TrainingSession> findByUserId(Long userId);
}
