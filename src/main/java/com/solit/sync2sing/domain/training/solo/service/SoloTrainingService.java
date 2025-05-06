package com.solit.sync2sing.domain.training.solo.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

public interface SoloTrainingService {

    SessionDTO getSession(UserDetails userDetails);

    SessionDTO createSession(UserDetails userDetails, CreateSessionRequest createSessionRequest);

    void deleteSession(UserDetails userDetails);

    SongListDTO getSongList(String type);

    SongListDTO.SongDTO getSong(Long songId, String type);

}
