package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.entity.BaseEntity;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.TrainingMode;
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
@Table(name = "vocal_analysis_report")
public class VocalAnalysisReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_training_report_id")
    private VocalAnalysisReport preTrainingReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "training_mode", nullable = false, length = 10)
    private TrainingMode trainingMode;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "report_type", nullable = false, length = 10)
    private RecordingContext reportType;

    @NotNull
    @Column(name = "pitch_score", nullable = false)
    private Integer pitchScore;

    @NotNull
    @Column(name = "beat_score", nullable = false)
    private Integer beatScore;

    @NotNull
    @Column(name = "pronunciation_score", nullable = false)
    private Integer pronunciationScore;

    @NotNull
    @Column(name = "breath_score", nullable = false)
    private Integer breathScore;

    @Size(max = 255)
    @NotNull
    @Column(name = "overall_review_title", nullable = false)
    private String overallReviewTitle;

    @Size(max = 1000)
    @NotNull
    @Column(name = "overall_review_content", nullable = false, length = 1000)
    private String overallReviewContent;

    @Size(max = 1000)
    @Column(name = "cause_content", length = 1000)
    private String causeContent;

    @Size(max = 1000)
    @Column(name = "proposal_content", length = 1000)
    private String proposalContent;

    @Size(max = 255)
    @Column(name = "feedback_title")
    private String feedbackTitle;

    @Size(max = 1000)
    @Column(name = "feedback_content", length = 1000)
    private String feedbackContent;

}