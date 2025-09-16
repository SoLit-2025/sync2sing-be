package com.solit.sync2sing.domain.training.duet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptPartnerApplicationAndCreateSessionResponse {

    private Long roomId;

    private Long hostTrainingSessionId;
    private Long partnerTrainingSessionId;

    private LocalDateTime preRecordingDueDate;
    private LocalDateTime postRecordingDueDate;

    private String status;
}
