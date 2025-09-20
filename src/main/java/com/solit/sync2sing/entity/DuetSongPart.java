package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.entity.BaseEntity;
import com.solit.sync2sing.global.type.VoiceType;
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
@Table(name = "duet_song_part")
public class DuetSongPart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @NotNull
    @Column(name = "part_number", nullable = false)
    private Integer partNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "part_name", nullable = false)
    private String partName;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "voice_type", nullable = false, length = 50)
    private VoiceType voiceType;

    @NotNull
    @Column(name = "pitch_note_min", nullable = false, length = 10)
    private String pitchNoteMin;

    @NotNull
    @Column(name = "pitch_note_max", nullable = false, length = 10)
    private String pitchNoteMax;

}