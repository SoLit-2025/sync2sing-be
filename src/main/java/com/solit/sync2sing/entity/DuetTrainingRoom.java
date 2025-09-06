package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.entity.BaseEntity;
import com.solit.sync2sing.global.type.DuetTrainingRoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "duet_training_room")
public class DuetTrainingRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_training_session_id")
    private TrainingSession hostTrainingSession;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_training_session_id")
    private TrainingSession partnerTrainingSession;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_user_part_id", nullable = false)
    private DuetSongPart hostUserPart;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_user_part_id", nullable = false)
    private DuetSongPart partnerUserPart;

    @NotNull
    @Column(name = "curriculum_days", nullable = false)
    private Integer curriculumDays;

    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    private DuetTrainingRoomStatus status = DuetTrainingRoomStatus.PENDING;

    @Setter
    @Column(name = "pre_recording_due_date")
    private LocalDateTime preRecordingDueDate;

    @Setter
    @Column(name = "post_recording_due_date")
    private LocalDateTime postRecordingDueDate;

}