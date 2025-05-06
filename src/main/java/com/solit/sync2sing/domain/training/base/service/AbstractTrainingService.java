package com.solit.sync2sing.domain.training.base.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.global.type.TrainingMode;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class AbstractTrainingService {

    private final TrainingMode trainingMode;

    public AbstractTrainingService(TrainingMode trainingMode) {
        this.trainingMode = trainingMode;
    }

//
//    SessionDTO getSession(UserDetails userDetails) {
//
//    }
//
//    SessionDTO createSession(UserDetails userDetails, CreateSessionRequest createSessionRequest) {
//
//    }
//
//    void deleteSession(UserDetails userDetails) {
//
//    }
//
//    SongListDTO getSongList(String type) {
//
//    }
//
//    SongListDTO.SongDTO getSong(Long songId, String type) {
//
//    }
}
