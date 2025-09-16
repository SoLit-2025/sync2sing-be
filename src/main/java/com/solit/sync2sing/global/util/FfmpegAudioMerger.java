package com.solit.sync2sing.global.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.solit.sync2sing.global.response.ResponseCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FfmpegAudioMerger {

    private final S3Util s3Util;

    /**
     * 두 입력 오디오 URL을 FFmpeg로 모노 믹스(amix)하여 stdout으로 내보내고,
     * 그 스트림을 즉시 S3에 멀티파트 업로드합니다.
     *
     * @param input1Url  FFmpeg가 직접 읽을 수 있는 URL(https 권장; private이면 presigned GET 사용)
     * @param input2Url  FFmpeg가 직접 읽을 수 있는 URL(https 권장; private이면 presigned GET 사용)
     * @return 업로드된 S3 URL (예: https://{bucket}.s3.amazonaws.com/audios/merged/...)
     */
    public String mergeToMonoAndUpload(String input1Url, String input2Url) {
        // FFmpeg 명령: 두 트랙을 amix로 섞고, 라우드니스 평탄화(dynaudnorm) 후 MP3로 인코딩
        List<String> cmd = List.of(
                "ffmpeg",
                "-hide_banner", "-nostdin", "-y",
                "-i", input1Url,
                "-i", input2Url,
                "-filter_complex",
                "[0:a]volume=1.0[a0];[1:a]volume=1.0[a1];" +
                        "[a0][a1]amix=inputs=2:duration=longest:dropout_transition=2," +
                        "dynaudnorm[out]",
                "-map", "[out]",
                "-c:a", "libmp3lame", "-b:a", "192k",
                "-f", "mp3",
                "pipe:1" // stdout
        );

        Process p = null;
        Thread errDrainer = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            // stderr는 따로 드레인(redirectErrorStream=false 유지; stdout은 바이너리 오디오 스트림)
            pb.redirectErrorStream(false);
            p = pb.start();

            final InputStream errStream = p.getErrorStream();

            // FFmpeg stderr 드레인 (버퍼 가득 차서 블로킹되는 것 방지)
            errDrainer = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(errStream))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        log.debug("[ffmpeg] {}", line);
                    }
                } catch (IOException ignore) {}
            }, "ffmpeg-stderr-drainer");
            errDrainer.setDaemon(true);
            errDrainer.start();

            // stdout(오디오) → S3로 멀티파트 업로드
            try (InputStream ffmpegOut = p.getInputStream()) {
                // audios/merged/UUID.mp3 로 업로드
                String s3Url = s3Util.saveMergedAudioStreamToS3(ffmpegOut);

                // FFmpeg 종료 코드 확인
                int exit = p.waitFor();
                if (exit != 0) {
                    log.warn("ffmpeg exited with non-zero code {}", exit);
                    throw new ResponseStatusException(
                            ResponseCode.AUDIO_MERGE_FAILED.getStatus(),
                            ResponseCode.AUDIO_MERGE_FAILED.getMessage()
                    );
                }
                return s3Url;
            }
        } catch (IOException | InterruptedException e) {
            if (p != null) p.destroyForcibly();
            throw new ResponseStatusException(
                    ResponseCode.AUDIO_MERGE_FAILED.getStatus(),
                    ResponseCode.AUDIO_MERGE_FAILED.getMessage()
            );
        } finally {
            if (errDrainer != null) {
                try { errDrainer.join(2000); } catch (InterruptedException ignore) {}
            }
        }
    }
}
