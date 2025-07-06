package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.SignupResponseDTO;
import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.UserSignupService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SignupServiceImplementation implements UserSignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SignupServiceImplementation(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SignupResponseDTO signUp(SignupRequestDTO requestDTO) {
        validateRequest(requestDTO);

        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());

        User userEntity = User.builder()
                .username(requestDTO.getUsername())
                .password(encodedPassword)
                .nickname(requestDTO.getNickname())
                .gender(Gender.FEMALE)
                .age(26)
                .pitchNoteMin("C3")
                .pitchNoteMax("G5")
                .voiceType(VoiceType.SOPRANO)
                .duetPenaltyCount(0)
                .duetPenaltyUntil(null)
                .roles(List.of("USER"))
                .build();

        try {
            userRepository.save(userEntity);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    ResponseCode.DUPLICATE_EMAIL.getStatus(),
                    ResponseCode.DUPLICATE_EMAIL.getMessage()
            );
        }

        return SignupResponseDTO.builder()
                .username(userEntity.getUsername())
                .nickname(userEntity.getNickname())
                .gender(userEntity.getGender())
                .age(userEntity.getAge())
                .pitchNoteMin(userEntity.getPitchNoteMin())
                .pitchNoteMax(userEntity.getPitchNoteMax())
                .voiceType(userEntity.getVoiceType())
                .duetPenaltyUntil(null)
                .build();
    }


    private void validateRequest(SignupRequestDTO requestDTO) {
        if (requestDTO.getUsername() == null || requestDTO.getPassword() == null || requestDTO.getNickname() == null) {
            throw new ResponseStatusException(
                    ResponseCode.SIGNUP_REQUIRED_FIELDS.getStatus(),
                    ResponseCode.SIGNUP_REQUIRED_FIELDS.getMessage()
            );
        }
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new ResponseStatusException(
                    ResponseCode.DUPLICATE_EMAIL.getStatus(),
                    ResponseCode.DUPLICATE_EMAIL.getMessage()
            );
        }
    }
}