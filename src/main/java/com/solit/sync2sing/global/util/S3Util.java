package com.solit.sync2sing.global.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.solit.sync2sing.global.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String saveAlbumCoverToS3(MultipartFile imageFile) {
        return saveFileToS3(imageFile, "images/album-cover");
    }

    public String saveMrAudioToS3(MultipartFile audioFile) {
        return saveFileToS3(audioFile, "audios/mr");
    }

    public String saveOriginalAudioToS3(MultipartFile audioFile) {
        return saveFileToS3(audioFile, "audios/original");
    }

    public String saveRecordingAudioToS3(MultipartFile audioFile) {
        return saveFileToS3(audioFile, "audios/recordings");
    }

    public String saveMergedAudioStreamToS3(InputStream input) {
        return uploadStreamMultipart(input, "audios/merged", "mp3", "audio/mpeg");
    }

    public String uploadStreamMultipart(InputStream input, String folderPath, String fileExt, String contentType) {
        if (input == null) {
            throw new ResponseStatusException(
                    ResponseCode.EMPTY_FILE_EXCEPTION.getStatus(),
                    ResponseCode.EMPTY_FILE_EXCEPTION.getMessage()
            );
        }

        // s3Key 생성 (기존 saveFileToS3와 동일한 규칙: folder/UUID.ext)
        final String storeFilename = UUID.randomUUID() + (fileExt != null && !fileExt.isBlank() ? "." + fileExt : "");
        final String s3Key = folderPath.endsWith("/") ? folderPath + storeFilename : folderPath + "/" + storeFilename;

        // initiate
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(contentType);

        InitiateMultipartUploadRequest initReq = new InitiateMultipartUploadRequest(bucket, s3Key)
                .withObjectMetadata(meta);

        InitiateMultipartUploadResult initRes = null;
        final List<PartETag> partETags = new ArrayList<>();

        // 최소 5MB 이상 (멀티파트 제약). 8MB 권장
        final int PART_SIZE = 8 * 1024 * 1024;

        try {
            initRes = amazonS3.initiateMultipartUpload(initReq);
            String uploadId = initRes.getUploadId();

            byte[] buf = new byte[PART_SIZE];
            int off = 0;
            int partNumber = 1;

            while (true) {
                int read = input.read(buf, off, buf.length - off);
                if (read == -1) {
                    if (off > 0) {
                        // 마지막 파트 업로드
                        partETags.add(uploadOnePart(bucket, s3Key, uploadId, partNumber++, Arrays.copyOf(buf, off)));
                    }
                    break;
                }
                off += read;
                if (off == buf.length) {
                    partETags.add(uploadOnePart(bucket, s3Key, uploadId, partNumber++, buf));
                    buf = new byte[PART_SIZE];
                    off = 0;
                }
            }

            if (partETags.isEmpty()) {
                amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, s3Key, initRes.getUploadId()));
                throw new ResponseStatusException(
                        ResponseCode.AUDIO_MERGE_FAILED.getStatus(),
                        ResponseCode.AUDIO_MERGE_FAILED.getMessage()
                );
            }

            partETags.sort(Comparator.comparingInt(PartETag::getPartNumber));

            // complete
            CompleteMultipartUploadRequest compReq = new CompleteMultipartUploadRequest(
                    bucket, s3Key, uploadId, partETags
            );
            amazonS3.completeMultipartUpload(compReq);

            return amazonS3.getUrl(bucket, s3Key).toString();

        } catch (IOException e) {
            // abort
            if (initRes != null) {
                try {
                    amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, s3Key, initRes.getUploadId()));
                } catch (Exception ignore) {}
            }
            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_EXCEPTION.getStatus(),
                    ResponseCode.FILE_UPLOAD_EXCEPTION.getMessage()
            );
        } catch (RuntimeException e) {
            if (initRes != null) {
                try {
                    amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, s3Key, initRes.getUploadId()));
                } catch (Exception ignore) {}
            }
            log.error("{}", e.getMessage(), e);
            throw new ResponseStatusException(
                    ResponseCode.INTERNAL_ERROR.getStatus(),
                    ResponseCode.INTERNAL_ERROR.getMessage()
            );
        }
    }

    private PartETag uploadOnePart(String bucket, String key, String uploadId, int partNumber, byte[] bytes) {
        UploadPartRequest partReq = new UploadPartRequest()
                .withBucketName(bucket)
                .withKey(key)
                .withUploadId(uploadId)
                .withPartNumber(partNumber)
                .withInputStream(new ByteArrayInputStream(bytes))
                .withPartSize(bytes.length);
        UploadPartResult partRes = amazonS3.uploadPart(partReq);
        return partRes.getPartETag();
    }

    private String saveFileToS3(MultipartFile multipartFile, String folderPath){
        if(multipartFile.isEmpty() || Objects.isNull(multipartFile.getOriginalFilename())){
            throw new ResponseStatusException(
                    ResponseCode.EMPTY_FILE_EXCEPTION.getStatus(),
                    ResponseCode.EMPTY_FILE_EXCEPTION.getMessage()
            );
        }

        String originalFilename = multipartFile.getOriginalFilename();

        String storeFilename = UUID.randomUUID() + "." + extractExt(originalFilename);

        String s3Key = folderPath.endsWith("/")
                ? folderPath + storeFilename
                : folderPath + "/" + storeFilename;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try {
            amazonS3.putObject(bucket, s3Key, multipartFile.getInputStream(), metadata);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_EXCEPTION.getStatus(),
                    ResponseCode.FILE_UPLOAD_EXCEPTION.getMessage()
            );
        }

        return amazonS3.getUrl(bucket, s3Key).toString();
    }

    public void deleteFileFromS3(String fileUrl) {
        String splitStr = ".com/";
        String fileName = fileUrl.substring(fileUrl.lastIndexOf(splitStr) + splitStr.length());

        amazonS3.deleteObject(bucket, fileName);
    }

    public void deletetranscriptFileFromS3(String fileUrl) {
        String splitStr = ".com/sync2sing-bucket/";
        String fileName = fileUrl.substring(fileUrl.lastIndexOf(splitStr) + splitStr.length());

        amazonS3.deleteObject(bucket, fileName);
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}
