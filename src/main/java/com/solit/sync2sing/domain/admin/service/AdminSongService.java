package com.solit.sync2sing.domain.admin.service;

import com.solit.sync2sing.domain.admin.dto.AdminSoloSongUploadRequest;
import com.solit.sync2sing.domain.training.base.dto.SongListDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AdminSongService {

    void adminSoloSongUpload(MultipartFile albumCoverImage, MultipartFile originalAudio, MultipartFile mrAudio, AdminSoloSongUploadRequest request);

}
