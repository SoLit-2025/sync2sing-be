package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.TrainingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingImageRepository extends JpaRepository<TrainingImage, Long> {
    List<TrainingImage> findByTrainingIdOrderByImageOrder(Long trainingId);
}
