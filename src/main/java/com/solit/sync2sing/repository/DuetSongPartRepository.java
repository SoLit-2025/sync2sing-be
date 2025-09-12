package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.DuetSongPart;
import com.solit.sync2sing.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DuetSongPartRepository extends JpaRepository<DuetSongPart, Long> {
    List<DuetSongPart> findBySong(Song song);
    Optional<DuetSongPart> findBySongIdAndPartNumber(Long songId, Integer partNumber);
}
