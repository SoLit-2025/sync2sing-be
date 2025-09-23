package com.solit.sync2sing.domain.training.base.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.domain.training.common.dto.CurriculumListResponse;
import com.solit.sync2sing.domain.training.common.dto.TrainingDTO;
import com.solit.sync2sing.domain.training.duet.dto.DuetTrainingRoomListResponse;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.type.*;
import com.solit.sync2sing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractTrainingService {

    private final TrainingMode trainingMode;

    private final UserRepository userRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionTrainingRepository trainingSessionTrainingRepository;
    private final RecordingRepository recordingRepository;
    private final DuetTrainingRoomRepository duetTrainingRoomRepository;
    private final SongRepository songRepository;
    private final LyricslineRepository lyricslineRepository;
    private final DuetSongPartRepository duetSongPartRepository;

    @Transactional
    public Optional<SessionDTO> getSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.USER_NOT_FOUND.getStatus(),
                        ResponseCode.USER_NOT_FOUND.getMessage()
                ));

        // 내 세션 조회
        TrainingSession mySession;
        Optional<TrainingSession> mySessionOpt = trainingSessionRepository
                .findByUserId(userId).stream()
                .filter(s -> s.getTrainingMode() == trainingMode)
                .findFirst();

        if (mySessionOpt.isEmpty()) {
            return Optional.empty();
        }

        mySession = mySessionOpt.get();

        // 훈련 데드라인이 지났는지 확인
        LocalDate deadlineDate = mySession.getCurriculumEndDate().toLocalDate();
        boolean deadlineReached = LocalDate.now().isAfter(deadlineDate);

        if (deadlineReached && mySession.getStatus() != SessionStatus.AFTER_TRAINING) {
            mySession.setStatus(SessionStatus.AFTER_TRAINING);
            trainingSessionRepository.save(mySession);
        }

        // SongDTO 빌드 (SOLO: id/title/artist, DUET: + userPartName)
        Song song = mySession.getSong();
        SongListDTO.SongDTO.SongDTOBuilder songB = SongListDTO.SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist());

        LocalDateTime preDue = null, postDue = null;
        Optional<Recording> preRec = Optional.empty();
        Optional<Recording> postRecOpt = Optional.empty();
        DuetTrainingRoomListResponse.DuetTrainingRoomDto.DuetTrainingRoomDtoBuilder DuetTrainingRoomDtoB = null;

        if (trainingMode == TrainingMode.DUET) {
            // DuetTrainingRoom 에서 사용자가 참가 중인 듀엣 방 정보, 파트 이름, 마감일 꺼내기
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

            preRec = recordingRepository
                    .findByTrainingSessionAndRecordingPhaseAndRecordingFormat(mySession, RecordingContext.PRE, RecordingFormat.MERGED);
            postRecOpt = recordingRepository
                    .findByTrainingSessionAndRecordingPhaseAndRecordingFormat(mySession, RecordingContext.POST, RecordingFormat.MERGED);


            if (LocalDate.now().isAfter(ChronoLocalDate.from(preDue)) && preRec.isEmpty()) {
                user.setDuetPenaltyCount(user.getDuetPenaltyCount() + 1);
                user.setDuetPenaltyUntil(LocalDate.now().plusDays(3).atStartOfDay());

                deleteRoom(room.getId());

                return Optional.empty();
            }

            if (LocalDate.now().isAfter(ChronoLocalDate.from(postDue)) && postRecOpt.isEmpty()) {
                user.setDuetPenaltyCount(user.getDuetPenaltyCount() + 1);
                user.setDuetPenaltyUntil(LocalDate.now().plusDays(3).atStartOfDay());

                deleteRoom(room.getId());

                return Optional.empty();
            }

            DuetSongPart hostPartEntity = room.getHostUserPart();
            DuetSongPart partnerPartEntity = room.getPartnerUserPart();

            DuetTrainingRoomListResponse.DuetPartDTO hostPartDTO =
                    DuetTrainingRoomListResponse.DuetPartDTO.builder()
                            .partNumber(hostPartEntity.getPartNumber())
                            .partName(hostPartEntity.getPartName())
                            .voiceType(hostPartEntity.getVoiceType().name())
                            .pitchNoteMin(hostPartEntity.getPitchNoteMin())
                            .pitchNoteMax(hostPartEntity.getPitchNoteMax())
                            .build();

            DuetTrainingRoomListResponse.DuetPartDTO partnerPartDTO =
                    DuetTrainingRoomListResponse.DuetPartDTO.builder()
                            .partNumber(partnerPartEntity.getPartNumber())
                            .partName(partnerPartEntity.getPartName())
                            .voiceType(partnerPartEntity.getVoiceType().name())
                            .pitchNoteMin(partnerPartEntity.getPitchNoteMin())
                            .pitchNoteMax(partnerPartEntity.getPitchNoteMax())
                            .build();

            DuetTrainingRoomDtoB = DuetTrainingRoomListResponse.DuetTrainingRoomDto.builder()
                    .id(room.getId())
                    .createdAt(room.getCreatedAt())
                    .trainingDays(room.getCurriculumDays())
                    .hostPart(hostPartDTO)
                    .partnerPart(partnerPartDTO);
        }

        SongListDTO.SongDTO songDTO = songB.build();

        // 커리큘럼 매핑
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
        List<TrainingDTO> vocalList  = new ArrayList<>(curriculumMap.get(TrainingCategory.PRONUNCIATION));

        CurriculumListResponse curriculum = CurriculumListResponse.builder()
                .pitch(pitchList)
                .rhythm(rhythmList)
                .pronunciation(vocalList)
                .build();

        // SessionDTO 빌드
        SessionDTO.SessionDTOBuilder dtoB = SessionDTO.builder()
                .sessionId(mySession.getId())
                .status(mySession.getStatus())
                .startDate(mySession.getCurriculumStartDate())
                .endDate(mySession.getCurriculumEndDate())
                .trainingDays(mySession.getCurriculumDays())
                .song(songDTO)
                .curriculum(curriculum);

        if (trainingMode == TrainingMode.DUET) {
            dtoB.preRecordingDueDate(preDue)
                .postRecordingDueDate(postDue)
                .preRecordingFileUrl(
                    preRec
                        .map(r -> r.getAudioFile().getFileUrl())
                        .orElse(null)
                )
                .postRecordingFileUrl(
                    postRecOpt
                        .map(r -> r.getAudioFile().getFileUrl())
                        .orElse(null)
                );

            DuetTrainingRoomListResponse.SongDTO duetSongDTO = DuetTrainingRoomListResponse.SongDTO.builder()
                    .id(song.getId())
                    .title(song.getTitle())
                    .artist(song.getArtist())
                    .albumArtUrl(song.getAlbumCoverFile().getFileUrl())
                    .build();
            DuetTrainingRoomListResponse.DuetTrainingRoomDto duetTrainingRoomDto = DuetTrainingRoomDtoB.song(duetSongDTO).build();
            dtoB.duetTrainingRoom(duetTrainingRoomDto);
        }

        return Optional.of(dtoB.build());

    }

    protected void deleteRoom(Long roomId) {
        // DuetTrainingService에서 구현
    }

    @Transactional
    public SessionDTO createSession(Long userId, CreateSessionRequest request) {
        // trainingDays 유효성 검사 (3, 7, 14만 허용)
        int days = request.getTrainingDays();
        if (days != 3 && days != 7 && days != 14) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_CURRICULUM_DAYS.getStatus(),
                    ResponseCode.INVALID_CURRICULUM_DAYS.getMessage()
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.USER_NOT_FOUND.getStatus(),
                        ResponseCode.USER_NOT_FOUND.getMessage()
                ));

        // 1) 요청한 Song 조회
        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                ResponseCode.SONG_NOT_FOUND.getStatus(),
                                ResponseCode.SONG_NOT_FOUND.getMessage()
                        )
                );

        // 2) 세션 엔티티 생성
        LocalDateTime start = trainingMode == TrainingMode.SOLO ? LocalDateTime.now() : LocalDateTime.now().plusDays(3);
        LocalDateTime end   = start.plusDays(request.getTrainingDays() - 1);

        TrainingSession session = TrainingSession.builder()
                .user(user)
                .song(song)
                .trainingMode(trainingMode)
                .curriculumStartDate(start)
                .curriculumEndDate(end)
                .curriculumDays(request.getTrainingDays())
                .status(SessionStatus.BEFORE_TRAINING)
                .build();

        trainingSessionRepository.save(session);

        // 3) 반환 DTO 빌드
        SongListDTO.SongDTO songDto = SongListDTO.SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .build();

        return SessionDTO.builder()
                .sessionId(session.getId())
                .status(session.getStatus())
                .startDate(session.getCurriculumStartDate())
                .endDate(session.getCurriculumEndDate())
                .trainingDays(session.getCurriculumDays())
                .song(songDto)
                .preRecordingFileUrl(null)
                .postRecordingFileUrl(null)
                .curriculum(null)
                .build();
    }

    @Transactional
    public void deleteSession(Long userId) {
        // 1) 현재 사용자·모드에 해당하는 세션 조회
        TrainingSession session = trainingSessionRepository.findByUserId(userId).stream()
                .filter(s -> s.getTrainingMode() == trainingMode)
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                ResponseCode.TRAINING_SESSION_NOT_FOUND.getStatus(),
                                ResponseCode.TRAINING_SESSION_NOT_FOUND.getMessage()
                        )
                );

        // 2) 세션 삭제 (cascade 규칙에 따라 연관된 녹음·훈련 데이터도 함께 삭제됨)
        trainingSessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public SongListDTO getSongList(String type) {
        boolean isMr = "mr".equalsIgnoreCase(type);
        boolean isOriginal = "original".equalsIgnoreCase(type);
        if (!isMr && !isOriginal) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getStatus(),
                    ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getMessage()
            );
        }

        // 1) 해당 모드의 모든 곡 조회
        List<Song> songs = songRepository.findByTrainingMode(trainingMode);

        // 2) DTO 매핑
        List<SongListDTO.SongDTO> songDTOList = songs.stream().map(song -> {
            // 2-1) 공통 정보 세팅
            SongListDTO.SongDTO.SongDTOBuilder songDTOBuilder = SongListDTO.SongDTO.builder()
                    .id(song.getId())
                    .title(song.getTitle())
                    .artist(song.getArtist())
                    .youtubeLink(song.getYoutubeLink());

            if (trainingMode == TrainingMode.SOLO) {
                songDTOBuilder
                        .voiceType(song.getVoiceType().name())
                        .pitchNoteMin(song.getPitchNoteMin())
                        .pitchNoteMax(song.getPitchNoteMax());
            }

            // 2-2) 전체 가사
            List<Lyricsline> allLines = lyricslineRepository
                    .findBySongOrderByLineIndex(song);
            List<SongListDTO.LyricLineDTO> lyrics = allLines.stream()
                    .map(l -> {
                        SongListDTO.LyricLineDTO.LyricLineDTOBuilder lyricLineDTOBuilder = SongListDTO.LyricLineDTO.builder()
                            .lineIndex(l.getLineIndex())
                            .text(l.getText())
                            .startTime(l.getStartTimeMs());

                        if (trainingMode == TrainingMode.DUET) {
                            lyricLineDTOBuilder.partNumber(l.getDuetSongPart().getPartNumber());
                        }

                        return lyricLineDTOBuilder.build();
                    })
                    .collect(Collectors.toList());
            songDTOBuilder.lyrics(lyrics);

            // 2-3) 파일 URL / 앨범 커버
            songDTOBuilder.albumArtUrl(song.getAlbumCoverFile().getFileUrl());
            if (isMr) {
                songDTOBuilder.fileUrl(song.getMrAudioFile().getFileUrl());
            } else {
                songDTOBuilder.fileUrl(song.getOriginalAudioFile().getFileUrl());
            }

            // 2-4) 듀엣 모드인 경우 파트 정보 조회
            if (trainingMode == TrainingMode.DUET) {
                List<DuetSongPart> parts = duetSongPartRepository.findBySong(song);
                List<SongListDTO.DuetPartDTO> duetPartList = parts.stream().map(part -> SongListDTO.DuetPartDTO.builder()
                        .partNumber(part.getPartNumber())
                        .partName(part.getPartName())
                        .voiceType(part.getVoiceType().name())
                        .pitchNoteMin(part.getPitchNoteMin())
                        .pitchNoteMax(part.getPitchNoteMax())
                        .build()).collect(Collectors.toList());
                songDTOBuilder.duetParts(duetPartList);
            }

            return songDTOBuilder.build();
        }).collect(Collectors.toList());

        // 3) 리스트 감싸서 반환
        return SongListDTO.builder()
                .songList(songDTOList)
                .build();
    }

    @Transactional(readOnly = true)
    public SongListDTO.SongDTO getSong(Long songId, String type) {
        boolean isMr = "mr".equalsIgnoreCase(type);
        boolean isOriginal = "original".equalsIgnoreCase(type);
        if (!isMr && !isOriginal) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getStatus(),
                    ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getMessage()
            );
        }

        // 1) 곡 조회
        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                ResponseCode.SONG_NOT_FOUND.getStatus(),
                                ResponseCode.SONG_NOT_FOUND.getMessage()
                        )
                );

        // 1-1) 모드 일치 검사
        if (song.getTrainingMode() != trainingMode) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getStatus(),
                    ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getMessage()
            );
        }

        // 2) DTO 빌더 준비
        SongListDTO.SongDTO.SongDTOBuilder songDTOBuilder = SongListDTO.SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .youtubeLink(song.getYoutubeLink());

        if (trainingMode == TrainingMode.SOLO) {
            songDTOBuilder
                    .voiceType(song.getVoiceType().name())
                    .pitchNoteMin(song.getPitchNoteMin())
                    .pitchNoteMax(song.getPitchNoteMax());
        }

        // 3) 전체 가사 조회
        List<Lyricsline> lines = lyricslineRepository.findBySongOrderByLineIndex(song);
        List<SongListDTO.LyricLineDTO> lyrics = lines.stream()
                .map(l -> {
                    SongListDTO.LyricLineDTO.LyricLineDTOBuilder lyricLineDTOBuilder = SongListDTO.LyricLineDTO.builder()
                            .lineIndex(l.getLineIndex())
                            .text(l.getText())
                            .startTime(l.getStartTimeMs());

                    if (trainingMode == TrainingMode.DUET) {
                        lyricLineDTOBuilder.partNumber(l.getDuetSongPart().getPartNumber());
                    }

                    return lyricLineDTOBuilder.build();
                })
                .collect(Collectors.toList());
        songDTOBuilder.lyrics(lyrics);

        // 4) 파일 URL 및 앨범 아트
        songDTOBuilder.albumArtUrl(song.getAlbumCoverFile().getFileUrl());
        if (isMr) {
            songDTOBuilder.fileUrl(song.getMrAudioFile().getFileUrl());
        } else {
            songDTOBuilder.fileUrl(song.getOriginalAudioFile().getFileUrl());
        }

        // 5) 듀엣 모드라면 파트별 인덱스 추가
        if (trainingMode == TrainingMode.DUET) {
            List<DuetSongPart> parts = duetSongPartRepository.findBySong(song);
            List<SongListDTO.DuetPartDTO> duetPartList = parts.stream().map(part -> SongListDTO.DuetPartDTO.builder()
                    .partNumber(part.getPartNumber())
                    .partName(part.getPartName())
                    .voiceType(part.getVoiceType().name())
                    .pitchNoteMin(part.getPitchNoteMin())
                    .pitchNoteMax(part.getPitchNoteMax())
                    .build()).collect(Collectors.toList());
            songDTOBuilder.duetParts(duetPartList);
        }

        return songDTOBuilder.build();
    }
}
