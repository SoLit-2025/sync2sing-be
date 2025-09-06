package com.solit.sync2sing.domain.training.common.service;

import com.solit.sync2sing.domain.training.common.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface TrainingService {
    
    CurriculumListResponse generateTrainingCurriculum(Long userId, GenerateCurriculumRequest generateCurriculumRequest);

    SetTrainingProgressResponse setTrainingProgress(Long userId, SetTrainingProgressRequest setTrainingProgressRequest, Long sessionId, Long trainingId);

    CurrentTrainingListDTO getCurrentTrainingList(Long userId);

    GenerateVocalAnalysisReportResponse generateVocalAnalysisReport(Long userId, MultipartFile vocalFile, GenerateVocalAnalysisReportRequest request);
}
