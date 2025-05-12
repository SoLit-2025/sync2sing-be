package com.solit.sync2sing.domain.user.service;

import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.SignupResponseDTO;


public interface UserSignupService {
    SignupResponseDTO signUp(SignupRequestDTO requestDTO);

}
