package com.solit.sync2sing.global.transcription.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solit.sync2sing.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class transcriptionService {

    private final S3Util s3Util;

    private final TranscribeClient transcribeClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Async
    public CompletableFuture<String> transcribeAndGetText(String jobName, String audioS3Url) {
        startTranscription(jobName, audioS3Url);
        TranscriptionJob job;
        int attempts = 0;

        do {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            job = getJob(jobName);
            attempts++;
        } while (job.transcriptionJobStatus() != TranscriptionJobStatus.COMPLETED && attempts < 10);

        if (job.transcriptionJobStatus() != TranscriptionJobStatus.COMPLETED) {
            throw new IllegalStateException("Transcription job did not complete in time");
        }

        String transcriptUrl = job.transcript().transcriptFileUri();
        String transcriptText = getTranscriptText(transcriptUrl);

        s3Util.deletetranscriptFileFromS3(transcriptUrl);

        return CompletableFuture.completedFuture(transcriptText);
    }

    public String startTranscription(String jobName, String mediaUri) {
        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .media(Media.builder().mediaFileUri(mediaUri).build())
                .mediaFormat("wav")
                .identifyLanguage(true)
                .outputBucketName(bucket)
                .build();

        transcribeClient.startTranscriptionJob(request);
        return jobName;
    }

    public TranscriptionJob getJob(String jobName) {
        GetTranscriptionJobRequest request = GetTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .build();

        return transcribeClient.getTranscriptionJob(request).transcriptionJob();
    }

    public String getTranscriptText(String transcriptFileUri) {
        try {
            String json = restTemplate.getForObject(transcriptFileUri, String.class);
            JsonNode node = objectMapper.readTree(json);
            return node.path("results").path("transcripts").get(0).path("transcript").asText();
        } catch (Exception e) {
            throw new RuntimeException("Transcribe 결과 파싱 실패", e);
        }
    }

    public TranscriptionJobStatus checkStatus(String jobName) {
        return transcribeClient.getTranscriptionJob(
                GetTranscriptionJobRequest.builder()
                        .transcriptionJobName(jobName)
                        .build()
        ).transcriptionJob().transcriptionJobStatus();
    }

    public String getTranscriptUrl(String jobName) {
        return transcribeClient.getTranscriptionJob(
                GetTranscriptionJobRequest.builder()
                        .transcriptionJobName(jobName)
                        .build()
        ).transcriptionJob().transcript().transcriptFileUri();
    }
}
