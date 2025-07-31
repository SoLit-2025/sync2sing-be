package com.solit.sync2sing.domain.training.base.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    private Long songId;
    private Integer keyAdjustment;
    private Integer trainingDays;
}
