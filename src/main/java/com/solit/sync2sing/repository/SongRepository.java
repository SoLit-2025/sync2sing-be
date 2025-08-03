package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.Song;
import com.solit.sync2sing.global.type.TrainingMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {
    Optional<Song> findByTitle(String title);
    List<Song> findByTrainingMode(TrainingMode trainingMode);
}
