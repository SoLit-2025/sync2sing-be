package com.solit.sync2sing.global.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.solit.sync2sing.global.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Util {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String saveAlbumCoverToS3(MultipartFile imageFile) {
        return saveFileToS3(imageFile, "images/album-cover");
    }

    public String saveMrAudioToS3(MultipartFile imageFile) {
        return saveFileToS3(imageFile, "audios/mr");
    }

    public String saveOriginalAudioToS3(MultipartFile imageFile) {
        return saveFileToS3(imageFile, "audios/original");
    }

    public String saveRecordingAudioToS3(MultipartFile imageFile) {
        return saveFileToS3(imageFile, "audios/recordings");
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

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}
