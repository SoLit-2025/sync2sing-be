package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.type.DuetTrainingRoomStatus;
import com.solit.sync2sing.global.type.VoiceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "duet_training_room")
public class DuetTrainingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_training_session_id", nullable = false)
    private TrainingSession hostTrainingSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_training_session_id")
    private TrainingSession partnerTrainingSession;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_user_part_id", nullable = false)
    private DuetSongPart hostUserPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_user_part_id")
    private DuetSongPart partnerUserPart;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_voice_type_preference", length = 50)
    private VoiceType partnerVoiceTypePreference;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    private DuetTrainingRoomStatus status = DuetTrainingRoomStatus.PENDING;

    @NotNull
    @Column(name = "pre_recording_due_date", nullable = false)
    private LocalDateTime preRecordingDueDate;

    @NotNull
    @Column(name = "post_recording_due_date", nullable = false)
    private LocalDateTime postRecordingDueDate;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}