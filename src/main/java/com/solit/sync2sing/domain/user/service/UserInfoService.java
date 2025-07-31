package com.solit.sync2sing.domain.user.service;
import com.solit.sync2sing.domain.user.dto.response.UserInfoResponseDTO;

public interface UserInfoService {
    UserInfoResponseDTO getUserInfo(String username);
}
