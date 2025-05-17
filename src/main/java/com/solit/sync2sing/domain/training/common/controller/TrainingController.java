package com.solit.sync2sing.domain.training.common.controller;

import com.solit.sync2sing.global.security.CustomUserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;

import com.solit.sync2sing.domain.training.common.dto.*;
import com.solit.sync2sing.domain.training.common.service.TrainingService;
import com.solit.sync2sing.global.response.ResponseDTO;
import com.solit.sync2sing.global.response.ResponseCode;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }
    
    @PostMapping("/curriculum")
    public ResponseEntity<ResponseDTO> generateTrainingCurriculum(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody GenerateCurriculumRequest generateCurriculumRequest
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new ResponseDTO(
                ResponseCode.CURRICULUM_CREATED,
                trainingService.generateTrainingCurriculum(userDetails, generateCurriculumRequest)
            ));
    }

    @GetMapping("/training")
    public ResponseEntity<ResponseDTO> getCurrentTrainingList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ResponseDTO(
                ResponseCode.CURRENT_TRAINING_LIST_FETCHED,
                trainingService.getCurrentTrainingList(userDetails)
            ));
    }

    @PutMapping("/training/sessions/{session_id}/trainings/{training_id}/progress")
    public ResponseEntity<ResponseDTO> setTrainingProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sessionId,
            @PathVariable Long trainingId,
            @RequestBody SetTrainingProgressRequest setTrainingProgressRequest
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ResponseDTO(
                ResponseCode.TRAINING_PROGRESS_UPDATED,
                trainingService.setTrainingProgress(userDetails,setTrainingProgressRequest, sessionId, trainingId)
            ));
    }

    @PostMapping("/vocal-analysis")
    public ResponseEntity<ResponseDTO> generateVocalAnalysisReport(
            @RequestPart(value = "vocal_file") MultipartFile vocal_file,
            @RequestPart(value = "data") GenerateVocalAnalysisReportRequest generateVocalAnalysisReportRequest
    ) {
        // TODO: 보컬 파일 S3 업로드
        String recordingFileUrl = "";

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new ResponseDTO(
                ResponseCode.VOCAL_ANALYSIS_REPORT_CREATED,
                trainingService.generateVocalAnalysisReport(recordingFileUrl, generateVocalAnalysisReportRequest)
            ));
    }

}
