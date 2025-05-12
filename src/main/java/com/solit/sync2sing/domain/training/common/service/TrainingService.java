package com.solit.sync2sing.domain.training.common.service;

import com.solit.sync2sing.global.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.domain.training.common.dto.*;
import org.springframework.web.bind.annotation.PathVariable;

public interface TrainingService {
    
    CurriculumListResponse generateTrainingCurriculum(GenerateCurriculumRequest generateCurriculumRequest);

    TrainingDTO setTrainingProgress(SetTrainingProgressRequest setTrainingProgressRequest, Long sessionId, Long trainingId);

    CurrentTrainingListDTO getCurrentTrainingList();

    VocalAnalysisReportDTO generateVocalAnalysisReport(String recordingFileUrl, GenerateVocalAnalysisReportRequest generateVocalAnalysisReportRequest);

}
