package com.solit.sync2sing.domain.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String voiceType;

    @NotNull
    private String pitchNoteMin;

    @NotNull
    private String pitchNoteMax;

}
