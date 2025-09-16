package com.solit.sync2sing.domain.admin.controller;

import com.solit.sync2sing.domain.admin.dto.AdminDuetSongUploadRequest;
import com.solit.sync2sing.domain.admin.dto.AdminSongDeleteRequest;
import com.solit.sync2sing.domain.admin.dto.AdminSoloSongUploadRequest;
import com.solit.sync2sing.domain.admin.service.AdminSongService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/song")
public class AdminSongController {

    private final AdminSongService adminSongService;

    public AdminSongController(AdminSongService adminSongService) {
        this.adminSongService = adminSongService;
    }

    @PostMapping("/solo")
    public ResponseEntity<ResponseDTO> adminSoloSongUpload(
            @RequestPart("album_cover_image") MultipartFile albumCoverImage,
            @RequestPart("original_audio") MultipartFile originalAudio,
            @RequestPart("mr_audio") MultipartFile mrAudio,
            @RequestPart("data") AdminSoloSongUploadRequest data
    ) {
        try {
            adminSongService.adminSoloSongUpload(albumCoverImage, originalAudio, mrAudio, data);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDTO(
                            ResponseCode.ADMIN_SOLOSONG_UPLOADED
                    ));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_EXCEPTION.getStatus(),
                    ResponseCode.FILE_UPLOAD_EXCEPTION.getMessage()
            );
        }
    }

    @PostMapping("/duet")
    public ResponseEntity<ResponseDTO> adminDuetSongUpload(
            @RequestPart("album_cover_image") MultipartFile albumCoverImage,
            @RequestPart("original_audio") MultipartFile originalAudio,
            @RequestPart("mr_audio") MultipartFile mrAudio,
            @RequestPart("data") AdminDuetSongUploadRequest data
    ) {
        try {
            adminSongService.adminDuetSongUpload(albumCoverImage, originalAudio, mrAudio, data);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDTO(
                            ResponseCode.ADMIN_DUETSONG_UPLOADED
                    ));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_EXCEPTION.getStatus(),
                    ResponseCode.FILE_UPLOAD_EXCEPTION.getMessage()
            );
        }
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO> adminSongDelete(
            @RequestBody AdminSongDeleteRequest request
    ) {
        try {
            adminSongService.adminSongDelete(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDTO(
                            ResponseCode.ADMIN_SONG_DELETED
                    ));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    ResponseCode.SONG_NOT_FOUND.getStatus(),
                    ResponseCode.SONG_NOT_FOUND.getMessage()
            );
        }
    }

}
