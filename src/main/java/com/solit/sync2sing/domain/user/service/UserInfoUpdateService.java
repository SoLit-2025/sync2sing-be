package com.solit.sync2sing.domain.user.service;

import com.solit.sync2sing.domain.user.dto.request.UserInfoUpdateRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.UserInfoUpdateResponseDTO;

public interface UserInfoUpdateService {
    UserInfoUpdateResponseDTO updateUserInfo(String username, UserInfoUpdateRequestDTO requestDTO);
}
