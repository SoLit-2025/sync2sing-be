package com.solit.sync2sing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

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

    @Size(max = 50)
    @Column(name = "partner_voice_type_preference", length = 50)
    private String partnerVoiceTypePreference;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @NotNull
    @Column(name = "pre_recording_due_date", nullable = false)
    private Instant preRecordingDueDate;

    @NotNull
    @Column(name = "post_recording_due_date", nullable = false)
    private Instant postRecordingDueDate;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}