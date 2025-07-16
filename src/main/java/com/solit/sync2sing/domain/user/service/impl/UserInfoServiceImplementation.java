package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.response.UserInfoResponseDTO;
import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class UserInfoServiceImplementation implements UserInfoService {

    private final UserRepository userRepository;

    @Override
    public UserInfoResponseDTO getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.USER_NOT_FOUND.getStatus(),
                        ResponseCode.USER_NOT_FOUND.getMessage()));

        return UserInfoResponseDTO.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .age(user.getAge())
                .voiceRange(user.getVoiceRange())
                .voiceType(user.getVoiceType())
                .duetPenaltyCount(user.getDuetPenaltyCount())
                .duetPenaltyUntil(user.getDuetPenaltyUntil())
                //.totalTrainingCount(user.getTotalTrainingCount(0))
                //.totalTrainingMinutes(user.getTotalTrainingMinutes())
                .build();
    }
}
