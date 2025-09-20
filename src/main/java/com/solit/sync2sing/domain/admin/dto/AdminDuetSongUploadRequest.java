package com.solit.sync2sing.domain.admin.dto;

import com.solit.sync2sing.global.type.VoiceType;
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
public class AdminDuetSongUploadRequest {

    @NotNull
    private String title;

    @NotNull
    private String artist;

    @NotNull
    @Valid
    private List<DuetPartDTO> duetParts;

    @NotNull
    @Valid
    private List<LyricLineDTO> lyrics;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuetPartDTO {
        @NotNull
        private Integer partNumber;

        @NotNull
        private String partName;

        @NotNull
        private String voiceType;

        @NotNull
        private String  pitchNoteMin;

        @NotNull
        private String  pitchNoteMax;
    }

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

        @NotNull
        private Integer partNumber;
    }

}
