package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.DuetRoomApplication;
import com.solit.sync2sing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DuetRoomApplicationRepository extends JpaRepository<DuetRoomApplication, Long> {
    List<DuetRoomApplication> findByApplicantUser(User user);
    List<DuetRoomApplication> findByDuetTrainingRoomId(Long duetTrainingRoomId);
    Optional<DuetRoomApplication> findByIdAndDuetTrainingRoomId(Long id, Long duetTrainingRoomId);
    void deleteByDuetTrainingRoomId(Long duetTrainingRoomId);
}
