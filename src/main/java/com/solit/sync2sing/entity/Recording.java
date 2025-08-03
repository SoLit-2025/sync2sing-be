package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.entity.BaseEntity;
import com.solit.sync2sing.global.type.RecordingContext;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "audio_file_id", nullable = false)
    private AudioFile audioFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSession trainingSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocal_analysis_report_id")
    private VocalAnalysisReport vocalAnalysisReport;

    @Size(max = 20)
    @NotNull
    @Column(name = "recording_format", nullable = false, length = 20)
    private String recordingFormat;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "recording_phase", nullable = false, length = 20)
    private RecordingContext recordingPhase;

}