package com.solit.sync2sing.domain.training.common.service;

import org.springframework.security.core.userdetails.UserDetails;
import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.domain.training.common.dto.*;

public interface TrainingService {
    
    CurriculumListResponse generateTrainingCurriculum(GenerateCurriculumRequest generateCurriculumRequest);

    TrainingDTO setTrainingProgress(UserDetails userDetails, SetTrainingProgressRequest setTrainingProgressRequest);

    CurrentTrainingListDTO getCurrentTrainingList(UserDetails userDetails);

    VocalAnalysisReportDTO generateVocalAnalysisReport(String recordingFileUrl, GenerateVocalAnalysisReportRequest generateVocalAnalysisReportRequest);

}
