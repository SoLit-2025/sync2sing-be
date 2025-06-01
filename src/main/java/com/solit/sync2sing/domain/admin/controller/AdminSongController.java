package com.solit.sync2sing.domain.admin.controller;

import com.solit.sync2sing.domain.admin.dto.AdminSoloSongDeleteRequest;
import com.solit.sync2sing.domain.admin.dto.AdminSoloSongUploadRequest;
import com.solit.sync2sing.domain.admin.service.AdminSongService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/solo")
    public ResponseEntity<ResponseDTO> adminSoloSongDelete(
            @RequestBody AdminSoloSongDeleteRequest request
    ) {
        try {
            adminSongService.adminSoloSongDelete(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDTO(
                            ResponseCode.ADMIN_SOLOSONG_DELETED
                    ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
