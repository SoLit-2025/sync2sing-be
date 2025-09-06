package com.solit.sync2sing.domain.training.duet.service.impl;

import com.solit.sync2sing.domain.training.base.dto.CreateSessionRequest;
import com.solit.sync2sing.domain.training.base.dto.SessionDTO;
import com.solit.sync2sing.domain.training.base.service.AbstractTrainingService;
import com.solit.sync2sing.domain.training.common.dto.GenerateVocalAnalysisReportRequest;
import com.solit.sync2sing.domain.training.common.dto.GenerateVocalAnalysisReportResponse;
import com.solit.sync2sing.domain.training.common.service.impl.TrainingServiceImpl;
import com.solit.sync2sing.domain.training.duet.dto.*;
import com.solit.sync2sing.domain.training.duet.service.DuetTrainingService;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.*;
import com.solit.sync2sing.global.util.FfmpegAudioMerger;
import com.solit.sync2sing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.List;

@Service
public class DuetTrainingServiceImpl extends AbstractTrainingService implements DuetTrainingService {

    private final TransactionTemplate transactionTemplate;

    private final TrainingServiceImpl trainingServiceImpl;

    private final DuetRoomApplicationRepository duetRoomApplicationRepository;
    private final DuetTrainingRoomRepository duetTrainingRoomRepository;
    private final DuetSongPartRepository duetSongPartRepository;
    private final SongRepository songRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final RecordingRepository recordingRepository;
    private final FfmpegAudioMerger ffmpegAudioMerger;
    private final VocalAnalysisReportRepository vocalAnalysisReportRepository;

    public DuetTrainingServiceImpl(
            TransactionTemplate transactionTemplate,
            TrainingSessionRepository trainingSessionRepository,
            TrainingSessionTrainingRepository trainingSessionTrainingRepository,
            RecordingRepository recordingRepository,
            DuetTrainingRoomRepository duetTrainingRoomRepository,
            SongRepository songRepository,
            LyricslineRepository lyricslineRepository,
            DuetSongPartRepository duetSongPartRepository,
            DuetRoomApplicationRepository duetRoomApplicationRepository,
            FfmpegAudioMerger ffmpegAudioMerger,
            TrainingServiceImpl trainingServiceImpl,
            VocalAnalysisReportRepository vocalAnalysisReportRepository
    ) {
        super(
                TrainingMode.DUET,
                trainingSessionRepository,
                trainingSessionTrainingRepository,
                recordingRepository,
                duetTrainingRoomRepository,
                songRepository,
                lyricslineRepository,
                duetSongPartRepository
        );

        this.transactionTemplate = transactionTemplate;
        this.duetRoomApplicationRepository = duetRoomApplicationRepository;
        this.duetTrainingRoomRepository = duetTrainingRoomRepository;
        this.duetSongPartRepository = duetSongPartRepository;
        this.songRepository = songRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.recordingRepository = recordingRepository;
        this.ffmpegAudioMerger = ffmpegAudioMerger;
        this.trainingServiceImpl = trainingServiceImpl;
        this.vocalAnalysisReportRepository = vocalAnalysisReportRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SentPartnerApplicationListResponse getSentPartnerApplications(CustomUserDetails userDetails) {
        List<DuetRoomApplication> duetRoomApplicationList = duetRoomApplicationRepository.findByApplicantUser(userDetails.getUser());

        List<SentPartnerApplicationListResponse.ApplicationDTO> ApplicationDTOList = duetRoomApplicationList.stream().map(duetRoomApplication ->
                SentPartnerApplicationListResponse.ApplicationDTO.builder()
                    .id(duetRoomApplication.getId())
                    .roomId(duetRoomApplication.getDuetTrainingRoom().getId())
                    .requestedAt(duetRoomApplication.getCreatedAt())
                    .build()).toList();

        return SentPartnerApplicationListResponse.builder()
                .applicationList(ApplicationDTOList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DuetTrainingRoomListResponse getRoomList() {
        List<DuetTrainingRoom> rooms =
                duetTrainingRoomRepository.findAllByStatusOrderByCreatedAtDesc(
                        DuetTrainingRoomStatus.PENDING
                );

        List<DuetTrainingRoomListResponse.DuetTrainingRoomDto> items = rooms.stream()
                .map(r -> DuetTrainingRoomListResponse.DuetTrainingRoomDto.builder()
                        .id(r.getId())
                        .createdAt(r.getCreatedAt())
                        .trainingDays(r.getCurriculumDays())
                        .song(DuetTrainingRoomListResponse.SongDTO.builder()
                                .id(r.getSong().getId())
                                .title(r.getSong().getTitle())
                                .artist(r.getSong().getArtist())
                                .albumArtUrl(
                                        r.getSong().getAlbumCoverFile() != null
                                                ? r.getSong().getAlbumCoverFile().getFileUrl()
                                                : null
                                )
                                .build())
                        .hostPartNumber(r.getHostUserPart().getPartNumber())
                        .hostPartName(r.getHostUserPart().getPartName())
                        .partnerPartNumber(r.getPartnerUserPart().getPartNumber())
                        .partnerPartName(r.getPartnerUserPart().getPartName())
                        .build())
                .toList();

        return DuetTrainingRoomListResponse.builder()
                .roomList(items)
                .build();
    }

    @Override
    @Transactional
    public DuetTrainingRoomListResponse.DuetTrainingRoomDto createRoom(CustomUserDetails userDetails, CreateRoomRequest request) {
        int days = request.getTrainingDays();
        if (days != 3 && days != 7 && days != 14) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_CURRICULUM_DAYS.getStatus(),
                    ResponseCode.INVALID_CURRICULUM_DAYS.getMessage()
            );
        }

        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.SONG_NOT_FOUND.getStatus(),
                        ResponseCode.SONG_NOT_FOUND.getMessage()
                ));

        Integer hostPartNumber = request.getHostUserPartNumber();
        Integer partnerPartNumber = hostPartNumber == 1 ? 0 : 1;

        DuetSongPart hostPart = duetSongPartRepository
                .findBySongIdAndPartNumber(request.getSongId(), hostPartNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.DUET_SONG_PART_NOT_FOUND.getStatus(),
                        ResponseCode.DUET_SONG_PART_NOT_FOUND.getMessage()
                ));

        DuetSongPart partnerPart = duetSongPartRepository
                .findBySongIdAndPartNumber(request.getSongId(), partnerPartNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.DUET_SONG_PART_NOT_FOUND.getStatus(),
                        ResponseCode.DUET_SONG_PART_NOT_FOUND.getMessage()
                ));

        DuetTrainingRoom room = DuetTrainingRoom.builder()
                .host(userDetails.getUser())
                .song(song)
                .hostUserPart(hostPart)
                .partnerUserPart(partnerPart)
                .curriculumDays(request.getTrainingDays())
                .status(DuetTrainingRoomStatus.PENDING)
                .build();

        room = duetTrainingRoomRepository.save(room);

        DuetTrainingRoomListResponse.SongDTO songDto = DuetTrainingRoomListResponse.SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .albumArtUrl(song.getAlbumCoverFile() != null ? song.getAlbumCoverFile().getFileUrl() : null)
                .build();

        return DuetTrainingRoomListResponse.DuetTrainingRoomDto.builder()
                .id(room.getId())
                .createdAt(room.getCreatedAt())
                .trainingDays(room.getCurriculumDays())
                .song(songDto)
                .hostPartNumber(room.getHostUserPart().getPartNumber())
                .hostPartName(room.getHostUserPart().getPartName())
                .partnerPartNumber(room.getPartnerUserPart().getPartNumber())
                .partnerPartName(room.getPartnerUserPart().getPartName())
                .build();
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        DuetTrainingRoom room = duetTrainingRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getStatus(),
                        ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getMessage()
                ));

        duetTrainingRoomRepository.delete(room);
        duetTrainingRoomRepository.flush();

        if (room.getHostTrainingSession() != null) {
            trainingSessionRepository.delete(room.getHostTrainingSession());
        }
        if (room.getPartnerTrainingSession() != null) {
            trainingSessionRepository.delete(room.getPartnerTrainingSession());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReceivedPartnerApplicationListResponse getReceivedPartnerApplications(CustomUserDetails userDetails, Long roomId) {
        List<DuetRoomApplication> duetRoomApplicationList = duetRoomApplicationRepository.findByDuetTrainingRoomId(roomId);

        List<ReceivedPartnerApplicationListResponse.ApplicationDTO> ApplicationDTOList = duetRoomApplicationList.stream().map(duetRoomApplication ->
                ReceivedPartnerApplicationListResponse.ApplicationDTO.builder()
                        .id(duetRoomApplication.getId())
                        .applicantId(duetRoomApplication.getApplicantUser().getId())
                        .applicantNickname(duetRoomApplication.getApplicantUser().getNickname())
                        .requestedAt(duetRoomApplication.getCreatedAt())
                        .build()).toList();

        return ReceivedPartnerApplicationListResponse.builder()
                .applicationList(ApplicationDTOList)
                .build();
    }

    @Override
    @Transactional
    public SentPartnerApplicationListResponse.ApplicationDTO createDuetRoomApplication(CustomUserDetails userDetails, Long roomId) {
        DuetTrainingRoom room = duetTrainingRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getStatus(),
                        ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getMessage()
                ));

        DuetRoomApplication duetRoomApplication = DuetRoomApplication.builder()
                .duetTrainingRoom(room)
                .applicantUser(userDetails.getUser())
                .build();

        duetRoomApplicationRepository.save(duetRoomApplication);

        return SentPartnerApplicationListResponse.ApplicationDTO.builder()
                .id(duetRoomApplication.getId())
                .roomId(roomId)
                .requestedAt(duetRoomApplication.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public AcceptPartnerApplicationAndCreateSessionResponse acceptPartnerApplicationAndCreateSession(CustomUserDetails userDetails, Long roomId, Long applicationId) {
        DuetTrainingRoom room = duetTrainingRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getStatus(),
                        ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getMessage()
                ));

        if (room.getStatus() != DuetTrainingRoomStatus.PENDING) {
            throw new ResponseStatusException(
                    ResponseCode.DUET_TRAINING_ROOM_NOT_PENDING.getStatus(),
                    String.format(ResponseCode.DUET_TRAINING_ROOM_NOT_PENDING.getMessage(), room.getStatus().name())
            );
        }

        // 호스트 권한 검증
        if (room.getHost() != null && !room.getHost().getId().equals(userDetails.getUser().getId())) {
            throw new ResponseStatusException(
                    ResponseCode.FORBIDDEN.getStatus(),
                    ResponseCode.FORBIDDEN.getMessage()
            );
        }

        // 신청서 확인 (해당 방의 신청서인지 확인)
        DuetRoomApplication app = duetRoomApplicationRepository.findByIdAndDuetTrainingRoomId(applicationId, roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.DUET_ROOM_APPLICATION_NOT_FOUND.getStatus(),
                        ResponseCode.DUET_ROOM_APPLICATION_NOT_FOUND.getMessage()
                ));

        // 세션 2개 생성 (호스트/파트너)
        CreateSessionRequest createSessionRequest = CreateSessionRequest.builder()
                .songId(room.getSong().getId())
                .trainingDays(room.getCurriculumDays())
                .build();

        SessionDTO hostSessionDTO = createSession(room.getHost(), createSessionRequest);
        SessionDTO partnerSessionDTO = createSession(app.getApplicantUser(), createSessionRequest);

        TrainingSession hostSession = trainingSessionRepository.findById(hostSessionDTO.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getStatus(),
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getMessage()
                ));
        TrainingSession partnerSession = trainingSessionRepository.findById(partnerSessionDTO.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getStatus(),
                        ResponseCode.TRAINING_SESSION_NOT_FOUND.getMessage()
                ));


        // 방에 세션/기한/상태/파트너 파트 반영
        room.setHostTrainingSession(hostSession);
        room.setPartnerTrainingSession(partnerSession);

        room.setPreRecordingDueDate(hostSession.getCurriculumStartDate());           // = 커리큘럼 시작일 (오늘 + 3일)
        room.setPostRecordingDueDate(hostSession.getCurriculumEndDate().plusDays(3)); // = 커리큘럼 종료일 + 3일
        room.setStatus(DuetTrainingRoomStatus.IN_SESSION);

        duetTrainingRoomRepository.save(room);

        // 해당 방의 신청서 전체 삭제
        duetRoomApplicationRepository.deleteByDuetTrainingRoomId(room.getId());

        return AcceptPartnerApplicationAndCreateSessionResponse.builder()
                .roomId(room.getId())
                .hostTrainingSessionId(hostSession.getId())
                .partnerTrainingSessionId(partnerSession.getId())
                .preRecordingDueDate(room.getPreRecordingDueDate())
                .postRecordingDueDate(room.getPostRecordingDueDate())
                .status(room.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public void deletePartnerApplication(CustomUserDetails userDetails, Long roomId, Long applicationId) {
        DuetRoomApplication duetRoomApplication = duetRoomApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.PARTNER_APPLICATION_NOT_FOUND.getStatus(),
                        ResponseCode.PARTNER_APPLICATION_NOT_FOUND.getMessage()
                ));

        duetRoomApplicationRepository.delete(duetRoomApplication);
    }

    @Override
    public AudioMergeResponse mergeAudios(CustomUserDetails userDetails, Long roomId) {
        DuetTrainingRoom room = duetTrainingRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getStatus(),
                        ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getMessage()
                ));

        TrainingSession hostSession = room.getHostTrainingSession();
        TrainingSession partnerSession = room.getPartnerTrainingSession();

        RecordingContext phase = hostSession.getStatus().equals(SessionStatus.BEFORE_TRAINING)
                ? RecordingContext.PRE : RecordingContext.POST;

        Recording hostRecording = recordingRepository.findByTrainingSessionAndRecordingPhaseAndRecordingFormat(hostSession, phase, RecordingFormat.SINGLE)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.RECORDING_NOT_FOUND.getStatus(),
                        ResponseCode.RECORDING_NOT_FOUND.getMessage()
                ));
        Recording partnerRecording = recordingRepository.findByTrainingSessionAndRecordingPhaseAndRecordingFormat(partnerSession, phase, RecordingFormat.SINGLE)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.RECORDING_NOT_FOUND.getStatus(),
                        ResponseCode.RECORDING_NOT_FOUND.getMessage()
                ));

        String hostAudioUrl = hostRecording.getAudioFile().getFileUrl();
        String partnerAudioUrl = partnerRecording.getAudioFile().getFileUrl();

        String mergedUrl = ffmpegAudioMerger.mergeToMonoAndUpload(hostAudioUrl, partnerAudioUrl);

        AudioFile mergedAudioFile = AudioFile.builder()
                .fileName(Path.of(mergedUrl).getFileName().toString())
                .fileUrl(mergedUrl)
                .build();

        VocalAnalysisReport hostVocalAnalysisReport = hostRecording.getVocalAnalysisReport();
        VocalAnalysisReport partnerVocalAnalysisReport = partnerRecording.getVocalAnalysisReport();

        int pitchAccuracyAvg = (hostVocalAnalysisReport.getPitchScore() + partnerVocalAnalysisReport.getPitchScore()) / 2;
        int beatAccuracyAvg = (hostVocalAnalysisReport.getBeatScore() + partnerVocalAnalysisReport.getBeatScore()) / 2;

        GenerateVocalAnalysisReportRequest vocalAnalysisReportRequest = GenerateVocalAnalysisReportRequest.builder()
                .trainingMode(TrainingMode.DUET.name())
                .analysisType(phase.name())
                .pitchAccuracy(pitchAccuracyAvg)
                .beatAccuracy(beatAccuracyAvg)
                .build();

        GenerateVocalAnalysisReportResponse vocalAnalysisReportResponse = trainingServiceImpl.duetMergedAnalysis(mergedUrl, vocalAnalysisReportRequest, userDetails);

        VocalAnalysisReport mergedVocalAnalysisReport = vocalAnalysisReportRepository.findById(vocalAnalysisReportResponse.getReportId())
                        .orElseThrow(() -> new ResponseStatusException(
                                ResponseCode.VOCAL_ANALYSIS_REPORT_NOT_FOUND.getStatus(),
                                ResponseCode.VOCAL_ANALYSIS_REPORT_NOT_FOUND.getMessage()
                        ));

        transactionTemplate.executeWithoutResult(status ->
            recordingRepository.save(Recording.builder()
                    .audioFile(mergedAudioFile)
                    .trainingSession(hostSession)
                    .vocalAnalysisReport(mergedVocalAnalysisReport)
                    .recordingFormat(RecordingFormat.MERGED)
                    .recordingPhase(phase)
                    .build())
        );

        return AudioMergeResponse.builder()
                .roomId(room.getId())
                .recordingPhase(phase.name())
                .mergedAudioUrl(mergedUrl)
                .vocalAnalysisReportResponse(vocalAnalysisReportResponse)
                .build();
    }
}
