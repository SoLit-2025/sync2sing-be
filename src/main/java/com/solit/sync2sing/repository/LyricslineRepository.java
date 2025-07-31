package com.solit.sync2sing.repository;

import com.solit.sync2sing.entity.DuetSongPart;
import com.solit.sync2sing.entity.Lyricsline;
import com.solit.sync2sing.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LyricslineRepository extends JpaRepository<Lyricsline, Long> {
    List<Lyricsline> findBySongOrderByLineIndex(Song song);
    List<Lyricsline> findByDuetSongPart(DuetSongPart duetSongPart);
}
