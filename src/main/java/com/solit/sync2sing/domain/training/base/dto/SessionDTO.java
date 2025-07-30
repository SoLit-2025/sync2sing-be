package com.solit.sync2sing.domain.training.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solit.sync2sing.domain.training.common.dto.CurriculumListResponse;
import com.solit.sync2sing.domain.training.common.dto.TrainingDTO;
import com.solit.sync2sing.global.type.SessionStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionDTO {
    private Long sessionId;
    private SessionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer trainingDays;
    private Integer keyAdjustment;
    private SongListDTO.SongDTO song;
    private String preRecordingFileUrl;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String postRecordingFileUrl;

    private CurriculumListResponse curriculum;

    // 듀엣 세션 DTO에만 사용되는 필드
    private LocalDateTime preRecordingDueDate;
    private LocalDateTime postRecordingDueDate;

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingSessionTrainingDTO extends TrainingDTO {
        private Integer progress;
        private boolean isCurrentTraining;
    }
}
