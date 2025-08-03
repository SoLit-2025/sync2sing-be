package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "lyricsline")
public class Lyricsline extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duet_song_part_id")
    private DuetSongPart duetSongPart;

    @NotNull
    @Column(name = "line_index", nullable = false)
    private Integer lineIndex;

    @Size(max = 1000)
    @NotNull
    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    @NotNull
    @Column(name = "start_time_ms", nullable = false)
    private Integer startTimeMs;

}