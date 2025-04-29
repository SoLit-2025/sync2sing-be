package com.solit.sync2sing.domain.user.service;

import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.SignupResponseDTO;
import com.solit.sync2sing.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static com.solit.sync2sing.global.response.ResponseCode.*;

public abstract class SignupService {

    @Autowired
    private UserRepository userRepository;

    protected void validateRequest(SignupRequestDTO requestDTO) {
        if (requestDTO.getUsername() == null || requestDTO.getPassword() == null || requestDTO.getNickname() == null) {
            throw new IllegalArgumentException(SIGNUP_REQUIRED_FIELDS.getMessage());
        }

        if (isEmailDuplicated(requestDTO.getUsername())) {
            throw new IllegalArgumentException(DUPLICATE_EMAIL.getMessage());
        }
    }

    private boolean isEmailDuplicated(String username) {
        return userRepository.existsByUsername(username);
    }


    public abstract SignupResponseDTO signUp(SignupRequestDTO requestDTO);
}