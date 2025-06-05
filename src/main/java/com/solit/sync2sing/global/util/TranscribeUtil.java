package com.solit.sync2sing.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

@Component
@RequiredArgsConstructor
public class TranscribeUtil {

    private final TranscribeClient transcribeClient;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

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
