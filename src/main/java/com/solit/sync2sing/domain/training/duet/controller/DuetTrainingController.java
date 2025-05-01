package com.solit.sync2sing.domain.training.duet.controller;

import com.solit.sync2sing.domain.training.dto.GenerateVocalAnalysisReportRequest;
import com.solit.sync2sing.domain.training.dto.SongListDTO;
import com.solit.sync2sing.domain.training.duet.dto.*;
import com.solit.sync2sing.domain.training.duet.service.DuetTrainingService;
import com.solit.sync2sing.domain.training.solo.dto.SetTrainingProgressRequest;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/duet-training")
public class DuetTrainingController {

    private final DuetTrainingService duetTrainingService;

    public DuetTrainingController(DuetTrainingService duetTrainingService) {
        this.duetTrainingService = duetTrainingService;
    }

    @PostMapping("/audios/merge")
    public ResponseEntity<ResponseDTO> mergeAudios(
            @RequestBody MergeAudioRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.DUET_AUDIO_MERGED,
                        duetTrainingService.mergeAudios(request.getRoomId())
                ));
    }

    @PostMapping("/partners")
    public ResponseEntity<ResponseDTO> applyForPartner(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreatePartnerApplicationRequest request
    ) {
        PartnerApplicationDTO response = duetTrainingService.applyForPartner(userDetails, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.PARTNER_APPLICATION_CREATED,
                        response
                ));
    }


    @GetMapping("/partners")
    public ResponseEntity<ResponseDTO> getPartnerApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String type
    ) {
        if ("receive".equals(type)) {
            List<ReceivedPartnerApplicationDTO> result = duetTrainingService.getReceivedPartnerApplications(userDetails);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(ResponseCode.RECEIVED_PARTNER_APPLICATIONS_FETCHED, result));
        } else if ("sent".equals(type)) {
            List<SentPartnerApplicationDTO> result = duetTrainingService.getSentPartnerApplications(userDetails);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(ResponseCode.SENT_PARTNER_APPLICATIONS_FETCHED, result));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/partners/{applicationId}")
    public ResponseEntity<ResponseDTO> acceptPartnerApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId
    ) {
        AcceptPartnerApplicationResponseDTO response = duetTrainingService.acceptPartnerApplication(userDetails, applicationId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.PARTNER_APPLICATION_ACCEPTED,
                        response
                ));
    }

    @DeleteMapping("/partners/{applicationId}")
    public ResponseEntity<ResponseDTO> rejectPartnerApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId
    ) {
        RejectPartnerApplicationResponseDTO response = duetTrainingService.rejectPartnerApplication(userDetails, applicationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.PARTNER_APPLICATION_REJECTED,
                        response
                ));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ResponseDTO> getRoomList() {
        List<DuetTrainingRoomDTO> roomList = duetTrainingService.getRoomList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_ROOMS_FETCHED,
                        roomList
                ));
    }

    @PostMapping("/rooms")
    public ResponseEntity<ResponseDTO> createRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateRoomRequest request
    ) {
        CreateRoomResponseDTO response = duetTrainingService.createRoom(userDetails, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_ROOM_CREATED,
                        response
                ));
    }

    @GetMapping("/session")
    public ResponseEntity<ResponseDTO> getSession(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DuetTrainingSessionResponseDTO response = duetTrainingService.getSession(userDetails);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_SESSION_FETCHED,
                        response
                ));
    }

    @DeleteMapping("/session")
    public ResponseEntity<ResponseDTO> endSession(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        EndSessionResponseDTO response = duetTrainingService.endSession(userDetails);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_SESSION_ENDED,
                        response
                ));
    }

    @GetMapping("/songs")
    public ResponseEntity<ResponseDTO> getSongList(
            @RequestParam String type
    ) {
        ResponseCode responseCode;
        if ("mr".equals(type)) {
            responseCode = ResponseCode.DUET_MR_SONG_LIST_FETCHED;
        } else if ("original".equals(type)) {
            responseCode = ResponseCode.DUET_ORIGINAL_SONG_LIST_FETCHED;
        } else {
            return ResponseEntity.badRequest().build();
        }

        List<SongListDTO.SongDTO> songList = duetTrainingService.getSongList(type);

        return ResponseEntity
                .status(responseCode.getStatus())
                .body(new ResponseDTO(
                        responseCode,
                        songList
                ));
    }

    @GetMapping("/songs/{songId}")
    public ResponseEntity<ResponseDTO> getSong(
            @PathVariable Long songId,
            @RequestParam String type
    ) {
        ResponseCode responseCode;
        if ("mr".equals(type)) {
            responseCode = ResponseCode.DUET_MR_SONG_FETCHED;
        } else if ("original".equals(type)) {
            responseCode = ResponseCode.DUET_ORIGINAL_SONG_FETCHED;
        } else {
            return ResponseEntity.badRequest().build();
        }

        SongListDTO.SongDTO song = duetTrainingService.getSong(songId, type);

        return ResponseEntity
                .status(responseCode.getStatus())
                .body(new ResponseDTO(
                        responseCode,
                        song
                ));
    }

    @GetMapping("/training")
    public ResponseEntity<ResponseDTO> getCurrentTraining(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<CurrentTrainingDTO> trainingList = duetTrainingService.getCurrentTraining(userDetails);

        if (trainingList == null || trainingList.isEmpty()) {
            return ResponseEntity
                    .status(204)
                    .body(new ResponseDTO(
                            ResponseCode.DUET_CURRENT_TRAINING_EMPTY
                    ));
        }

        return ResponseEntity
                .status(ResponseCode.DUET_CURRENT_TRAINING_FETCHED.getStatus())
                .body(new ResponseDTO(
                        ResponseCode.DUET_CURRENT_TRAINING_FETCHED,
                        trainingList
                ));
    }

    @PutMapping("/training/{trainingId}")
    public ResponseEntity<ResponseDTO> updateTrainingProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long trainingId,
            @RequestBody SetTrainingProgressRequest request
    ) {
        SetTrainingProgressResponseDTO response = duetTrainingService.updateTrainingProgress(
                userDetails, trainingId, request.getProgress()
        );
        return ResponseEntity
                .status(ResponseCode.DUET_TRAINING_PROGRESS_UPDATED.getStatus())
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_PROGRESS_UPDATED,
                        response
                ));
    }

    @PostMapping("/vocal-analysis")
    public ResponseEntity<ResponseDTO> generateVocalAnalysisReport(
            @RequestPart(value = "vocal_file") MultipartFile vocalFile,
            @RequestPart(value = "data") GenerateVocalAnalysisReportRequest request
    ) {
        // TODO: S3에 보컬 파일 업로드
        String recordingFileUrl = "";

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.VOCAL_ANALYSIS_REPORT_CREATED,
                        duetTrainingService.generateVocalAnalysisReport(recordingFileUrl, request)
                ));
    }
}
