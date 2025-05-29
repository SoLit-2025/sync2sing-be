package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.global.type.VoiceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_audio_file_id", nullable = false)
    private AudioFile originalAudioFile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mr_audio_file_id", nullable = false)
    private AudioFile mrAudioFile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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
    @NotNull
    @Column(name = "voice_type", nullable = false, length = 50)
    private VoiceType voiceType;

    @NotNull
    @Column(name = "pitch_note_min", nullable = false)
    private String pitch_note_min;

    @NotNull
    @Column(name = "pitch_note_max", nullable = false)
    private String pitch_note_max;

}