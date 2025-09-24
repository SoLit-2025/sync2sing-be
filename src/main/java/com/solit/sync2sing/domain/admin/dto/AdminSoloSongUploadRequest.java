package com.solit.sync2sing.domain.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSoloSongUploadRequest {

    @NotNull
    private String trainingMode;

    @NotNull
    private String title;

    @NotNull
    private String artist;

    @NotNull
    private String youtubeLink;

    @NotNull
    private String voiceType;

    @NotNull
    private String pitchNoteMin;

    @NotNull
    private String pitchNoteMax;

    @NotNull
    @Valid
    private List<LyricLineDTO> lyrics;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LyricLineDTO {
        @NotNull
        private Integer lineIndex;

        @NotNull
        private String text;

        @NotNull
        private Integer startTime;
    }

}
