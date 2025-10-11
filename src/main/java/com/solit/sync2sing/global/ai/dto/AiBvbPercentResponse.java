package com.solit.sync2sing.global.ai.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiBvbPercentResponse {

    private int status;
    private String message;
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Double bending;
        private Double vibrt;
        private Double breath;
    }
}
