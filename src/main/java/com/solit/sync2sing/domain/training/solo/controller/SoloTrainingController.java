package com.solit.sync2sing.domain.training.solo.controller;

import com.solit.sync2sing.domain.training.base.dto.CreateSessionRequest;
import com.solit.sync2sing.domain.training.base.dto.SessionDTO;
import com.solit.sync2sing.domain.training.solo.service.SoloTrainingService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;
import com.solit.sync2sing.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/solo-training")
public class SoloTrainingController {

    private final SoloTrainingService soloTrainingService;

    public SoloTrainingController(
            SoloTrainingService soloTrainingService
    ) {
        this.soloTrainingService = soloTrainingService;
    }

    
    @GetMapping("/session")
    public ResponseEntity<ResponseDTO> getSession(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Optional<SessionDTO> sessionDTOOpt = soloTrainingService.getSession(userDetails.getId());

        if (sessionDTOOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(
                            ResponseCode.SOLO_TRAINING_SESSION_FETCHED
                    ));

        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(
                        ResponseCode.SOLO_TRAINING_SESSION_FETCHED,
                        sessionDTOOpt.get()
                ));
    }

    @PostMapping("/session")
    public ResponseEntity<ResponseDTO> createSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateSessionRequest createSessionRequest
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new ResponseDTO(
                ResponseCode.SOLO_TRAINING_SESSION_CREATED,
                soloTrainingService.createSession(userDetails.getId(), createSessionRequest)
            ));
    }

    @DeleteMapping("/session")
    public ResponseEntity<ResponseDTO> deleteSession(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        soloTrainingService.deleteSession(userDetails.getId());
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

}
