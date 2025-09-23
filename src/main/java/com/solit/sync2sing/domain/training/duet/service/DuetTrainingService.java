package com.solit.sync2sing.domain.training.duet.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.domain.training.duet.dto.*;

import java.util.Optional;

public interface DuetTrainingService {

    SentPartnerApplicationListResponse getSentPartnerApplications(Long userId);

    DuetTrainingRoomListResponse getRoomList(Long userId);

    DuetTrainingRoomListResponse.DuetTrainingRoomDto createRoom(Long userId, CreateRoomRequest request);

    void deleteRoom(Long roomId);

    ReceivedPartnerApplicationListResponse getReceivedPartnerApplications(Long userId, Long roomId);

    SentPartnerApplicationListResponse.ApplicationDTO createDuetRoomApplication(Long userId, Long roomId);

    AcceptPartnerApplicationAndCreateSessionResponse acceptPartnerApplicationAndCreateSession(Long userId, Long roomId, Long applicationId);

    SessionDTO createSession(Long userId, CreateSessionRequest createSessionRequest);

    void deletePartnerApplication(Long userId, Long roomId, Long applicationId);

    AudioMergeResponse mergeAudios(Long userId, Long roomId);

    Optional<SessionDTO> getSession(Long userId);

    void deleteSession(Long userId);

    SongListDTO getSongList(String type);

    SongListDTO.SongDTO getSong(Long songId, String type);

}
