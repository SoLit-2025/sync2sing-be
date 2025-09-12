package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.entity.BaseEntity;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.RecordingFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "recording")
public class Recording extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "audio_file_id", nullable = false)
    private AudioFile audioFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSession trainingSession;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocal_analysis_report_id")
    private VocalAnalysisReport vocalAnalysisReport;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "recording_format", nullable = false, length = 20)
    private RecordingFormat recordingFormat;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "recording_phase", nullable = false, length = 20)
    private RecordingContext recordingPhase;

}