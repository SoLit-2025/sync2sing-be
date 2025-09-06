package com.solit.sync2sing.domain.training.duet.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.domain.training.duet.dto.*;
import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.global.security.CustomUserDetails;

import java.util.Optional;

public interface DuetTrainingService {

    SentPartnerApplicationListResponse getSentPartnerApplications(CustomUserDetails userDetails);

    DuetTrainingRoomListResponse getRoomList();

    DuetTrainingRoomListResponse.DuetTrainingRoomDto createRoom(CustomUserDetails userDetails, CreateRoomRequest request);

    void deleteRoom(Long roomId);

    ReceivedPartnerApplicationListResponse getReceivedPartnerApplications(CustomUserDetails userDetails, Long roomId);

    SentPartnerApplicationListResponse.ApplicationDTO createDuetRoomApplication(CustomUserDetails userDetails, Long roomId);

    AcceptPartnerApplicationAndCreateSessionResponse acceptPartnerApplicationAndCreateSession(CustomUserDetails userDetails, Long roomId, Long applicationId);

    SessionDTO createSession(User user, CreateSessionRequest createSessionRequest);

    void deletePartnerApplication(CustomUserDetails userDetails, Long roomId, Long applicationId);

    AudioMergeResponse mergeAudios(CustomUserDetails userDetails, Long roomId);

    Optional<SessionDTO> getSession(CustomUserDetails userDetails);

    void deleteSession(CustomUserDetails userDetails);

    SongListDTO getSongList(String type);

    SongListDTO.SongDTO getSong(Long songId, String type);

}
