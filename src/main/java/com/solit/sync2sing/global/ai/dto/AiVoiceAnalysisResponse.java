package com.solit.sync2sing.global.ai.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVoiceAnalysisResponse {

    private int status;
    private String message;
    private Data data;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private List<VoiceTypeRatio> top_voice_types;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceTypeRatio {
        private String type;
        private double ratio;
    }
}
