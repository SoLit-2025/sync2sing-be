package com.solit.sync2sing.domain.training.solo.service;

import com.solit.sync2sing.domain.training.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

public interface SoloTrainingService {

    CurriculumListResponse generateTrainingCurriculum(GenerateCurriculumRequest generateCurriculumRequest);

    SessionDTO getSession(UserDetails userDetails);

    SessionDTO createSession(UserDetails userDetails, CreateSessionRequest createSessionRequest);

    void deleteSession(UserDetails userDetails);

    SongListDTO getSongList(String type);

    SongListDTO.SongDTO getSong(Long songId, String type);

    CurrentTrainingListDTO getCurrentTrainingList(UserDetails userDetails);

    TrainingDTO setTrainingProgress(UserDetails userDetails, Long trainingId, Integer progress);

    VocalAnalysisReportDTO generateVocalAnalysisReport(String recordingFileUrl, GenerateVocalAnalysisReportRequest generateVocalAnalysisReportRequest);
}
