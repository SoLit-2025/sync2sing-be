package com.solit.sync2sing.domain.training.solo.controller;

import com.solit.sync2sing.domain.training.dto.CreateSessionRequest;
import com.solit.sync2sing.domain.training.dto.GenerateCurriculumRequest;
import com.solit.sync2sing.domain.training.dto.GenerateVocalAnalysisReportRequest;
import com.solit.sync2sing.domain.training.solo.dto.SetTrainingProgressRequest;
import com.solit.sync2sing.domain.training.solo.service.SoloTrainingService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/solo-training")
public class SoloTrainingController {

    private final SoloTrainingService soloTrainingService;

    public SoloTrainingController(
            SoloTrainingService soloTrainingService
    ) {
        this.soloTrainingService = soloTrainingService;
    }

    @PostMapping("/curriculum")
    public ResponseEntity<ResponseDTO> generateTrainingCurriculum(
            @RequestBody GenerateCurriculumRequest generateCurriculumRequest
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new ResponseDTO(
                ResponseCode.CURRICULUM_CREATED,
                soloTrainingService.generateTrainingCurriculum(generateCurriculumRequest)
            ));
    }

    @GetMapping("/session")
    public ResponseEntity<ResponseDTO> getSession(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ResponseDTO(
                ResponseCode.SOLO_TRAINING_SESSION_FETCHED,
                soloTrainingService.getSession(userDetails)
            ));
    }

    @PostMapping("/session")
    public ResponseEntity<ResponseDTO> createSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateSessionRequest createSessionRequest
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new ResponseDTO(
                ResponseCode.SOLO_TRAINING_SESSION_CREATED,
                soloTrainingService.createSession(userDetails, createSessionRequest)
            ));
    }

    @DeleteMapping("/session")
    public ResponseEntity<ResponseDTO> deleteSession(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        soloTrainingService.deleteSession(userDetails);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ResponseDTO(
                ResponseCode.SOLO_TRAINING_SESSION_DELETED
            ));
    }

    @GetMapping("/songs")
    public ResponseEntity<ResponseDTO> getSongList(
            @RequestParam String type
    ) {
        ResponseCode responseCode;
        if (type.equals("mr")) {
            responseCode = ResponseCode.SOLO_MR_SONG_LIST_FETCHED;
        } else if (type.equals("original")) {
            responseCode = ResponseCode.SOLO_ORIGINAL_SONG_LIST_FETCHED;
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ResponseDTO(
                responseCode,
                soloTrainingService.getSongList(type)
            ));
    }

    @GetMapping("/songs/{songId}")
    public ResponseEntity<ResponseDTO> getSong(
            @PathVariable Long songId,
            @RequestParam String type
    ) {
        ResponseCode responseCode;
        if (type.equals("mr")) {
            responseCode = ResponseCode.SOLO_MR_SONG_FETCHED;
        } else if (type.equals("original")) {
            responseCode = ResponseCode.SOLO_ORIGINAL_SONG_FETCHED;
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ResponseDTO(
                responseCode,
                soloTrainingService.getSong(songId, type)
            ));
    }

    @GetMapping("/training")
    public ResponseEntity<ResponseDTO> getCurrentTrainingList(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ResponseDTO(
                ResponseCode.SOLO_CURRENT_TRAINING_LIST_FETCHED,
                soloTrainingService.getCurrentTrainingList(userDetails)
            ));
    }

    @PutMapping("/training/{trainingId}")
    public ResponseEntity<ResponseDTO> setTrainingProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long trainingId,
            @RequestBody SetTrainingProgressRequest setTrainingProgressRequest
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new ResponseDTO(
                ResponseCode.SOLO_TRAINING_PROGRESS_UPDATED,
                soloTrainingService.setTrainingProgress(userDetails, trainingId, setTrainingProgressRequest.getProgress())
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
                soloTrainingService.generateVocalAnalysisReport(recordingFileUrl, generateVocalAnalysisReportRequest)
            ));
    }
}
