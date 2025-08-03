package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.entity.UserTrainingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTrainingLogRepository extends JpaRepository<UserTrainingLog, Long> {
    List<UserTrainingLog> findByUser(User user);
}
