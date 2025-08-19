package com.solit.sync2sing.domain.training.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumListResponse {
    private List<TrainingDTO> pitch;
    private List<TrainingDTO> rhythm;
    private List<TrainingDTO> pronunciation;
    private List<TrainingDTO> breath;
}
