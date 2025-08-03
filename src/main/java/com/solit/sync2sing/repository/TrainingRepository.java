package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.Training;
import com.solit.sync2sing.global.type.TrainingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByCategory(TrainingCategory category);
}
