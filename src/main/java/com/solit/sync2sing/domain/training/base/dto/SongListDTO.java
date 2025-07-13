package com.solit.sync2sing.domain.training.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class SongListDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SongDTO {
        private Long id;
        private String title;
        private String artist;

        // 듀엣곡 목록 DTO에만 사용되는 필드
        private String userPartName;
    }
}
