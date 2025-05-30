package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
}
