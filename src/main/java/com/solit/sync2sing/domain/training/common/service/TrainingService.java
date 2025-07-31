package com.solit.sync2sing.domain.training.common.service;

import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.domain.training.common.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface TrainingService {
    
    CurriculumListResponse generateTrainingCurriculum(CustomUserDetails userDetails, GenerateCurriculumRequest generateCurriculumRequest);

    TrainingDTO setTrainingProgress(CustomUserDetails userDetails, SetTrainingProgressRequest setTrainingProgressRequest, Long sessionId, Long trainingId);

    CurrentTrainingListDTO getCurrentTrainingList(CustomUserDetails userDetails);

    GenerateVocalAnalysisReportResponse generateVocalAnalysisReport(CustomUserDetails userDetails, MultipartFile vocalFile, GenerateVocalAnalysisReportRequest request);
}
