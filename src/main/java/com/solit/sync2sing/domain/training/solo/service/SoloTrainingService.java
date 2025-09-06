package com.solit.sync2sing.domain.training.solo.service;

import com.solit.sync2sing.domain.training.base.dto.*;

import java.util.Optional;

public interface SoloTrainingService {

    Optional<SessionDTO> getSession(Long userId);

    SessionDTO createSession(Long userId, CreateSessionRequest createSessionRequest);

    void deleteSession(Long userId);

    SongListDTO getSongList(String type);

    SongListDTO.SongDTO getSong(Long songId, String type);

}
