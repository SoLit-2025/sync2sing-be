package com.solit.sync2sing.domain.training.duet.controller;

import com.solit.sync2sing.domain.training.base.dto.SessionDTO;
import com.solit.sync2sing.domain.training.duet.dto.*;
import com.solit.sync2sing.domain.training.duet.service.DuetTrainingService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;
import com.solit.sync2sing.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/duet-training")
public class DuetTrainingController {

    private final DuetTrainingService duetTrainingService;

    public DuetTrainingController(
            DuetTrainingService duetTrainingService
    ) {
        this.duetTrainingService = duetTrainingService;
    }

    @GetMapping("/applications/sent")
    public ResponseEntity<ResponseDTO> getSentPartnerApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.SENT_PARTNER_APPLICATIONS_FETCHED,
                        duetTrainingService.getSentPartnerApplications(userDetails.getId())
                ));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ResponseDTO> getRoomList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_ROOMS_FETCHED,
                        duetTrainingService.getRoomList(userDetails.getId())
                ));
    }

    @PostMapping("/rooms")
    public ResponseEntity<ResponseDTO> createRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateRoomRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_ROOM_CREATED,
                        duetTrainingService.createRoom(userDetails.getId(), request)
                ));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ResponseDTO> deleteRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        duetTrainingService.deleteRoom(roomId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_ROOM_DELETED
                ));
    }

    @GetMapping("/rooms/{roomId}/applications")
    public ResponseEntity<ResponseDTO> getPartnerApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.RECEIVED_PARTNER_APPLICATIONS_FETCHED,
                        duetTrainingService.getReceivedPartnerApplications(userDetails.getId(), roomId)
                ));
    }

    @PostMapping("/rooms/{roomId}/applications")
    public ResponseEntity<ResponseDTO> createDuetRoomApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.PARTNER_APPLICATION_CREATED,
                        duetTrainingService.createDuetRoomApplication(userDetails.getId(), roomId)
                ));
    }

    @PostMapping("/rooms/{roomId}/applications/{applicationId}")
    public ResponseEntity<ResponseDTO> acceptPartnerApplicationAndCreateSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @PathVariable Long applicationId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.PARTNER_APPLICATION_ACCEPTED,
                        duetTrainingService.acceptPartnerApplicationAndCreateSession(userDetails.getId(), roomId, applicationId)
                ));
    }

    @DeleteMapping("/rooms/{roomId}/applications/{applicationId}")
    public ResponseEntity<ResponseDTO> deletePartnerApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @PathVariable Long applicationId
    ) {
        duetTrainingService.deletePartnerApplication(userDetails.getId(), roomId, applicationId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.PARTNER_APPLICATION_REJECTED
                ));
    }

    @PostMapping("/rooms/{roomId}/merge-audios")
    public ResponseEntity<ResponseDTO> mergeAudios(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        ResponseCode.DUET_AUDIO_MERGED,
                        duetTrainingService.mergeAudios(userDetails.getId(), roomId)
                ));
    }

    @GetMapping("/session")
    public ResponseEntity<ResponseDTO> getSession(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Optional<SessionDTO> sessionDTOOpt = duetTrainingService.getSession(userDetails.getId());

        if (sessionDTOOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(
                            ResponseCode.DUET_TRAINING_SESSION_FETCHED
                    ));

        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_SESSION_FETCHED,
                        sessionDTOOpt.get()
                ));
    }

    @DeleteMapping("/session")
    public ResponseEntity<ResponseDTO> deleteSession(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        duetTrainingService.deleteSession(userDetails.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.DUET_TRAINING_SESSION_DELETED
                ));
    }

    @GetMapping("/songs")
    public ResponseEntity<ResponseDTO> getSongList(
            @RequestParam String type
    ) {
        ResponseCode responseCode;
        if (type.equals("mr")) {
            responseCode = ResponseCode.DUET_MR_SONG_LIST_FETCHED;
        } else if (type.equals("original")) {
            responseCode = ResponseCode.DUET_ORIGINAL_SONG_LIST_FETCHED;
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity
            .status(responseCode.getStatus())
            .body(new ResponseDTO(
                responseCode,
                duetTrainingService.getSongList(type)
            ));
    }

    @GetMapping("/songs/{songId}")
    public ResponseEntity<ResponseDTO> getSong(
            @PathVariable Long songId,
            @RequestParam String type
    ) {
        ResponseCode responseCode;
        if (type.equals("mr")) {
            responseCode = ResponseCode.DUET_MR_SONG_FETCHED;
        } else if (type.equals("original")) {
            responseCode = ResponseCode.DUET_ORIGINAL_SONG_FETCHED;
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        responseCode,
                        duetTrainingService.getSong(songId, type)
                ));
    }

}
