package com.solit.sync2sing.domain.user.service;

import com.solit.sync2sing.domain.user.dto.request.LoginRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LoginResponseDTO;

public interface UserLoginService {
    LoginResponseDTO login(LoginRequestDTO requestDTO);
}
