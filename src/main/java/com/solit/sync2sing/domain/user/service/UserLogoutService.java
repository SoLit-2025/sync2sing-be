package com.solit.sync2sing.domain.user.service;

import com.solit.sync2sing.domain.user.dto.request.LogoutRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LogoutResponseDTO;

public interface UserLogoutService {

    LogoutResponseDTO logout(String accessToken, LogoutRequestDTO logoutRequestDTO);
}
