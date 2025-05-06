package com.solit.sync2sing.domain.training.duet.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.domain.training.common.dto.GenerateVocalAnalysisReportRequest;
import com.solit.sync2sing.domain.training.common.dto.VocalAnalysisReportDTO;
import com.solit.sync2sing.domain.training.duet.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface DuetTrainingService {

    AudioMergeResponseDTO mergeAudios(Long roomId);

    PartnerApplicationDTO applyForPartner(UserDetails userDetails, CreatePartnerApplicationRequest request);

    List<ReceivedPartnerApplicationDTO> getReceivedPartnerApplications(UserDetails userDetails);

    List<SentPartnerApplicationDTO> getSentPartnerApplications(UserDetails userDetails);

    AcceptPartnerApplicationResponseDTO acceptPartnerApplication(UserDetails userDetails, Long applicationId);

    RejectPartnerApplicationResponseDTO rejectPartnerApplication(UserDetails userDetails, Long applicationId);

    List<DuetTrainingRoomDTO> getRoomList();

    CreateRoomResponseDTO createRoom(UserDetails userDetails, CreateRoomRequest request);

    DuetTrainingSessionResponseDTO getSession(UserDetails userDetails);

    EndSessionResponseDTO endSession(UserDetails userDetails);

    List<SongListDTO.SongDTO> getSongList(String type);

    SongListDTO.SongDTO getSong(Long songId, String type);

    List<CurrentTrainingDTO> getCurrentTraining(UserDetails userDetails);

    SetTrainingProgressResponseDTO updateTrainingProgress(UserDetails userDetails, Long trainingId, int progress);

    VocalAnalysisReportDTO generateVocalAnalysisReport(String recordingFileUrl, GenerateVocalAnalysisReportRequest request);

}
