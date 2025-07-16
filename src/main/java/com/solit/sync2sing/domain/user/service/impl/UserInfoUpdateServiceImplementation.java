package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.request.UserInfoUpdateRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.UserInfoUpdateResponseDTO;
import com.solit.sync2sing.domain.user.service.UserInfoUpdateService;
import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserInfoUpdateServiceImplementation implements
        UserInfoUpdateService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserInfoUpdateResponseDTO updateUserInfo(String username, UserInfoUpdateRequestDTO requestDTO) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(ResponseCode.USER_NOT_FOUND.getStatus(),
                        ResponseCode.USER_NOT_FOUND.getMessage()));
        user.setNickname(requestDTO.getNickname());

        userRepository.save(user);

        return UserInfoUpdateResponseDTO.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .age(user.getAge())
                .voiceRange(user.getPitchNoteMin() + " ~ " + user.getPitchNoteMax())
                .voiceType(user.getVoiceType())
                .duetPenaltyCount(user.getDuetPenaltyCount())
                .duetPenaltyUntil(user.getDuetPenaltyUntil())
                //.totalTrainingMinutes(user.getTotalTrainingMinutes())
                //.totalTrainingCount(user.getTotalTrainingCount())
                .build();
    }

}
