package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.entity.BaseEntity;
import com.solit.sync2sing.global.type.SessionStatus;
import com.solit.sync2sing.global.type.TrainingMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "training_session")
public class TrainingSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "training_mode", nullable = false, length = 50)
    private TrainingMode trainingMode;

    @NotNull
    @Column(name = "key_adjustment", nullable = false)
    private Integer keyAdjustment;

    @NotNull
    @Column(name = "curriculum_days", nullable = false)
    private Integer curriculumDays;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    private SessionStatus status = SessionStatus.BEFORE_TRAINING;

    @NotNull
    @Column(name = "curriculum_start_date", nullable = false)
    private LocalDateTime curriculumStartDate;

    @NotNull
    @Column(name = "curriculum_end_date", nullable = false)
    private LocalDateTime curriculumEndDate;

}