package com.solit.sync2sing.domain.training.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetTrainingProgressRequest {

    @NotNull
    @Min(0)
    @Max(100)
    private int progress;

}
