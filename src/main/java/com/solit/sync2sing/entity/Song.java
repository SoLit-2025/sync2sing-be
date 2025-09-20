package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.entity.BaseEntity;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.global.type.VoiceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "song")
public class Song extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "original_audio_file_id", nullable = false)
    private AudioFile originalAudioFile;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "mr_audio_file_id", nullable = false)
    private AudioFile mrAudioFile;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "album_cover_file_id", nullable = false)
    private ImageFile albumCoverFile;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "training_mode", nullable = false, length = 10)
    private TrainingMode trainingMode;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 255)
    @NotNull
    @Column(name = "artist", nullable = false)
    private String artist;

    @Enumerated(EnumType.STRING)
    @Column(name = "voice_type", length = 50)
    private VoiceType voiceType;

    @Column(name = "pitch_note_min", length = 10)
    private String pitchNoteMin;

    @Column(name = "pitch_note_max", length = 10)
    private String pitchNoteMax;

    @OneToMany(
            mappedBy = "song",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DuetSongPart> duetSongParts = new ArrayList<>();

    @OneToMany(
            mappedBy = "song",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Lyricsline> lines = new ArrayList<>();

}