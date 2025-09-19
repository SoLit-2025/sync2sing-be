package com.solit.sync2sing.domain.admin.service;

import com.solit.sync2sing.domain.admin.dto.AdminDuetSongUploadRequest;
import com.solit.sync2sing.domain.admin.dto.AdminSongDeleteRequest;
import com.solit.sync2sing.domain.admin.dto.AdminSoloSongUploadRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AdminSongService {

    void adminSoloSongUpload(MultipartFile albumCoverImage, MultipartFile originalAudio, MultipartFile mrAudio, AdminSoloSongUploadRequest request);
    void adminDuetSongUpload(MultipartFile albumCoverImage, MultipartFile originalAudio, MultipartFile mrAudio, AdminDuetSongUploadRequest request);

    void adminSongDelete(AdminSongDeleteRequest request);
}
