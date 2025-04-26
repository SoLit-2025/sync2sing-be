package com.solit.sync2sing.domain.user;

import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.SignupResponseDTO;
import com.solit.sync2sing.domain.user.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.SignupService;
import com.solit.sync2sing.domain.user.service.UserSignupService;
import org.springframework.stereotype.Service;

@Service
public class SignupServiceImplementation extends SignupService implements UserSignupService {

    private final UserRepository userRepository;

    public SignupServiceImplementation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public SignupResponseDTO signUp(SignupRequestDTO requestDTO) {

        validateRequest(requestDTO);

        return SignupResponseDTO.builder()
                .username(requestDTO.getUsername())
                .nickname(requestDTO.getNickname())
                .gender(requestDTO.getGender())
                .age(requestDTO.getAge())
                .voiceRange(requestDTO.getVoiceRange())
                .voiceType(requestDTO.getVoiceType())
                .duetPenaltyCount(0)
                .duetPenaltyUntil(null)
                .build();
    }

}
