package com.solit.sync2sing.domain.training.solo.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.global.security.CustomUserDetails;

import java.util.Optional;

public interface SoloTrainingService {

    Optional<SessionDTO> getSession(CustomUserDetails userDetails);

    SessionDTO createSession(User user, CreateSessionRequest createSessionRequest);

    void deleteSession(CustomUserDetails userDetails);

    SongListDTO getSongList(String type);

    SongListDTO.SongDTO getSong(Long songId, String type);

}
