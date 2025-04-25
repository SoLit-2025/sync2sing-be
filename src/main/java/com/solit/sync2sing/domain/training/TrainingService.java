package com.solit.sync2sing.domain.training;

import com.solit.sync2sing.domain.training.dto.*;
import com.solit.sync2sing.global.type.TrainingMode;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class TrainingService {

    private final TrainingMode trainingMode;

    public TrainingService(TrainingMode trainingMode) {
        this.trainingMode = trainingMode;
    }

//    CurriculumListResponse generateTrainingCurriculum(GenerateCurriculumRequest generateCurriculumRequest) {
//
//    }
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
//
//    CurrentTrainingListDTO getCurrentTrainingList(UserDetails userDetails) {
//
//    }
//
//    TrainingDTO setTrainingProgress(UserDetails userDetails) {
//
//    }
//
//    VocalAnalysisReportDTO generateVocalAnalysisReport(GenerateVocalAnalysisReportRequest generateVocalAnalysisReportRequest) {
//
//    }
}
