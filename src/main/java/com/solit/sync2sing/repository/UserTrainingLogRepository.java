package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.UserTrainingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface UserTrainingLogRepository extends JpaRepository<UserTrainingLog, Long> {
    Set<Long> findTrainedTrainingIdsByUserId(Long userId);
}
