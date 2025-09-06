package com.solit.sync2sing.domain.training.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetTrainingProgressRequest {

    @NotNull
    private int progress;

}
