package com.solit.sync2sing.global.ai.service;

import com.solit.sync2sing.global.ai.dto.AiBvbPercentResponse;
import com.solit.sync2sing.global.ai.dto.AiVoiceAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient webClient;

    @Value("${ai.server.url}")
    private String aiServerUrl; // 예: http://ai-server:8000

    public Mono<AiVoiceAnalysisResponse> analyzeVoice(String s3Uri) {
        return webClient.post()
                .uri(aiServerUrl + "/ai/voice-analysis")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("s3_uri", s3Uri))
                .retrieve()
                .bodyToMono(AiVoiceAnalysisResponse.class);
    }

    @Async
    public CompletableFuture<AiVoiceAnalysisResponse> analyzeWithAiServer(String s3Uri) {
        return analyzeVoice(s3Uri)
                .toFuture();
    }

    public Mono<AiBvbPercentResponse> analyzeBvbPercent(String s3Uri) {
        return webClient.post()
                .uri(aiServerUrl + "/ai/voice-analysis-aihub")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("s3_uri", s3Uri))
                .retrieve()
                .bodyToMono(AiBvbPercentResponse.class);
    }

    @Async
    public CompletableFuture<AiBvbPercentResponse> analyzeBvbPercentAsync(String s3Uri) {
        return analyzeBvbPercent(s3Uri).toFuture();
    }
}
