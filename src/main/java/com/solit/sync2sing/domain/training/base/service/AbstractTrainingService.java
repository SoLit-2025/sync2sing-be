package com.solit.sync2sing.domain.training.base.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.domain.training.common.dto.CurriculumListResponse;
import com.solit.sync2sing.domain.training.common.dto.TrainingDTO;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.TrainingCategory;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.repository.DuetTrainingRoomRepository;
import com.solit.sync2sing.repository.RecordingRepository;
import com.solit.sync2sing.repository.TrainingSessionRepository;
import com.solit.sync2sing.repository.TrainingSessionTrainingRepository;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

public abstract class AbstractTrainingService {

    private final TrainingMode trainingMode;

    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionTrainingRepository trainingSessionTrainingRepository;
    private final RecordingRepository recordingRepository;
    private final DuetTrainingRoomRepository duetTrainingRoomRepository;

    public AbstractTrainingService(TrainingMode trainingMode, TrainingSessionRepository trainingSessionRepository, TrainingSessionTrainingRepository trainingSessionTrainingRepository, RecordingRepository recordingRepository, DuetTrainingRoomRepository duetTrainingRoomRepository) {
        this.trainingMode = trainingMode;
        this.trainingSessionRepository = trainingSessionRepository;
        this.trainingSessionTrainingRepository = trainingSessionTrainingRepository;
        this.recordingRepository = recordingRepository;
        this.duetTrainingRoomRepository = duetTrainingRoomRepository;
    }


    SessionDTO getSession(CustomUserDetails userDetails) {

        // 1) 내 세션 조회
        TrainingSession mySession = trainingSessionRepository
                .findByUser(userDetails.getUser()).stream()
                .filter(s -> s.getTrainingMode() == trainingMode)
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                ResponseCode.TRAINING_SESSION_NOT_FOUND.getStatus(),
                                ResponseCode.TRAINING_SESSION_NOT_FOUND.getMessage()
                        )
                );

        // 2) SongDTO 빌드 (SOLO: id/title/artist, DUET: + userPartName)
        Song song = mySession.getSong();
        SongListDTO.SongDTO.SongDTOBuilder songB = SongListDTO.SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist());

        LocalDateTime preDue = null, postDue = null;

        if (trainingMode == TrainingMode.DUET) {
            // 3) DuetTrainingRoom 에서 사용자가 참가 중인 듀엣 방 정보, 파트 이름, 마감일 꺼내기
            DuetTrainingRoom room = duetTrainingRoomRepository
                    .findByHostTrainingSessionOrPartnerTrainingSession(mySession, mySession)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getStatus(),
                                    ResponseCode.DUET_TRAINING_ROOM_NOT_FOUND.getMessage()
                            )
                    );

            // 사용자가 호스트인지 파트너인지에 따라 올바른 DuetSongPart 선택
            DuetSongPart myPart = room.getHostTrainingSession().equals(mySession)
                    ? room.getHostUserPart()
                    : room.getPartnerUserPart();

            songB.userPartName(myPart.getPartName());

            // 듀엣 녹음 마감일
            preDue  = room.getPreRecordingDueDate();
            postDue = room.getPostRecordingDueDate();
        }

        SongListDTO.SongDTO songDTO = songB.build();

        // 4) PRE / POST 녹음 URL 조회
        Recording preRec = recordingRepository
                .findByTrainingSessionAndRecordingPhase(mySession, RecordingContext.PRE)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                ResponseCode.RECORDING_NOT_FOUND.getStatus(),
                                ResponseCode.RECORDING_NOT_FOUND.getMessage()
                        )
                );
        Optional<Recording> postRecOpt = recordingRepository
                .findByTrainingSessionAndRecordingPhase(mySession, RecordingContext.POST);

        // 5) 커리큘럼 매핑
        List<TrainingSessionTraining> trainings =
                trainingSessionTrainingRepository.findByTrainingSession(mySession);

        // 카테고리별 빈 리스트
        Map<TrainingCategory, List<SessionDTO.TrainingSessionTrainingDTO>> curriculumMap =
                new EnumMap<>(TrainingCategory.class);
        for (TrainingCategory cat : TrainingCategory.values()) {
            curriculumMap.put(cat, new ArrayList<>());
        }

        // 각 엔티티를 DTO로 변환해 해당 카테고리에 추가
        for (TrainingSessionTraining tst : trainings) {
            TrainingDTO base = TrainingDTO.toDTO(tst.getTraining());
            SessionDTO.TrainingSessionTrainingDTO dto =
                    SessionDTO.TrainingSessionTrainingDTO.builder()
                            .id(base.getId())
                            .category(base.getCategory())
                            .title(base.getTitle())
                            .grade(base.getGrade())
                            .description(base.getDescription())
                            .trainingMinutes(base.getTrainingMinutes())
                            .progress(tst.getProgress())
                            .isCurrentTraining(tst.isCurrentTraining())
                            .build();

            TrainingCategory cat = tst.getTraining().getCategory();
            curriculumMap.get(cat).add(dto);
        }

        // up-cast 및 CurriculumListResponse 빌드
        List<TrainingDTO> pitchList  = new ArrayList<>(curriculumMap.get(TrainingCategory.PITCH));
        List<TrainingDTO> rhythmList = new ArrayList<>(curriculumMap.get(TrainingCategory.RHYTHM));
        List<TrainingDTO> vocalList  = new ArrayList<>(curriculumMap.get(TrainingCategory.VOCALIZATION));
        List<TrainingDTO> breathList = new ArrayList<>(curriculumMap.get(TrainingCategory.BREATH));

        CurriculumListResponse curriculum = CurriculumListResponse.builder()
                .pitch(pitchList)
                .rhythm(rhythmList)
                .vocalization(vocalList)
                .breath(breathList)
                .build();

        // 6) SessionDTO 빌드 (DUET인 경우 dueDate 추가)
        SessionDTO.SessionDTOBuilder dtoB = SessionDTO.builder()
                .status(mySession.getStatus())
                .startDate(mySession.getCurriculumStartDate())
                .endDate(mySession.getCurriculumEndDate())
                .trainingDays(mySession.getCurriculumDays())
                .keyAdjustment(mySession.getKeyAdjustment())
                .song(songDTO)
                .preRecordingFileUrl(preRec.getAudioFile().getFileUrl())
                .postRecordingFileUrl(
                        postRecOpt
                                .map(r -> r.getAudioFile().getFileUrl())
                                .orElse(null)
                )
                .curriculum(curriculum);

        if (trainingMode == TrainingMode.DUET) {
            dtoB.preRecordingDueDate(preDue)
                    .postRecordingDueDate(postDue);
        }

        return dtoB.build();

    }
//
//    SessionDTO createSession(UserDetails userDetails, CreateSessionRequest createSessionRequest) {
//
//    }
//
//    void deleteSession(UserDetails userDetails) {
//
//    }
//
//    SongListDTO getSongList(String type) {
//
//    }
//
//    SongListDTO.SongDTO getSong(Long songId, String type) {
//
//    }
}
