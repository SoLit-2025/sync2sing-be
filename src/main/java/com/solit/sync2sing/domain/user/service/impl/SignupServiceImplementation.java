package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.SignupResponseDTO;
import com.solit.sync2sing.domain.user.entity.UserEntity;
import com.solit.sync2sing.domain.user.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.SignupService;
import com.solit.sync2sing.domain.user.service.UserSignupService;
import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignupServiceImplementation extends SignupService implements UserSignupService {

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

        UserEntity userEntity = UserEntity.builder()
                .username(requestDTO.getUsername())
                .password(encodedPassword)
                .nickname(requestDTO.getNickname())
                .gender(Gender.FEMALE)
                .age(26)
                .voiceRange("C3~G5")
                .voiceType(VoiceType.SOPRANO)
                .duetPenaltyCount(0)
                .duetPenaltyUntil(null)
                .roles(List.of("USER"))
                .build();

        userRepository.save(userEntity);

        return SignupResponseDTO.builder()
                .username(userEntity.getUsername())
                .nickname(userEntity.getNickname())
                .gender(userEntity.getGender())
                .age(userEntity.getAge())
                .voiceRange(userEntity.getVoiceRange())
                .voiceType(userEntity.getVoiceType())
                .duetPenaltyUntil(null)
                .build();
    }
}
