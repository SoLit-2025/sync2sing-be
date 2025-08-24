package com.solit.sync2sing.domain.training.base.service;

import com.solit.sync2sing.domain.training.base.dto.*;
import com.solit.sync2sing.domain.training.common.dto.CurriculumListResponse;
import com.solit.sync2sing.domain.training.common.dto.TrainingDTO;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.SessionStatus;
import com.solit.sync2sing.global.type.TrainingCategory;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractTrainingService {

    private final TrainingMode trainingMode;

    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionTrainingRepository trainingSessionTrainingRepository;
    private final RecordingRepository recordingRepository;
    private final DuetTrainingRoomRepository duetTrainingRoomRepository;
    private final SongRepository songRepository;
    private final LyricslineRepository lyricslineRepository;
    private final DuetSongPartRepository duetSongPartRepository;

    public Optional<SessionDTO> getSession(CustomUserDetails userDetails) {

        // 1) 내 세션 조회
        TrainingSession mySession;
        Optional<TrainingSession> mySessionOpt = trainingSessionRepository
                .findByUser(userDetails.getUser()).stream()
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
        Optional<Recording> preRec = recordingRepository
                .findByTrainingSessionAndRecordingPhase(mySession, RecordingContext.PRE);
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
        List<TrainingDTO> vocalList  = new ArrayList<>(curriculumMap.get(TrainingCategory.PRONUNCIATION));
        List<TrainingDTO> breathList = new ArrayList<>(curriculumMap.get(TrainingCategory.BREATH));

        CurriculumListResponse curriculum = CurriculumListResponse.builder()
                .pitch(pitchList)
                .rhythm(rhythmList)
                .pronunciation(vocalList)
                .breath(breathList)
                .build();

        // 6) SessionDTO 빌드 (DUET인 경우 dueDate 추가)
        SessionDTO.SessionDTOBuilder dtoB = SessionDTO.builder()
                .sessionId(mySession.getId())
                .status(mySession.getStatus())
                .startDate(mySession.getCurriculumStartDate())
                .endDate(mySession.getCurriculumEndDate())
                .trainingDays(mySession.getCurriculumDays())
                .keyAdjustment(mySession.getKeyAdjustment())
                .song(songDTO)
                .preRecordingFileUrl(preRec
                        .map(r -> r.getAudioFile().getFileUrl())
                        .orElse(null)
                )
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

        return Optional.of(dtoB.build());

    }

    @Transactional
    public SessionDTO createSession(CustomUserDetails userDetails, CreateSessionRequest request) {
        // trainingDays 유효성 검사 (3, 7, 14만 허용)
        int days = request.getTrainingDays();
        if (days != 3 && days != 7 && days != 14) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_CURRICULUM_DAYS.getStatus(),
                    ResponseCode.INVALID_CURRICULUM_DAYS.getMessage()
            );
        }

        // TODO: getKeyAdjustment 유효성 검사

        // 1) 요청한 Song 조회
        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                ResponseCode.SONG_NOT_FOUND.getStatus(),
                                ResponseCode.SONG_NOT_FOUND.getMessage()
                        )
                );

        // 2) 세션 엔티티 생성
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = start.plusDays(request.getTrainingDays() - 1);

        TrainingSession session = TrainingSession.builder()
                .user(userDetails.getUser())
                .song(song)
                .trainingMode(trainingMode)
                .curriculumStartDate(start)
                .curriculumEndDate(end)
                .curriculumDays(request.getTrainingDays())
                .keyAdjustment(request.getKeyAdjustment())
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
                .status(session.getStatus())
                .startDate(session.getCurriculumStartDate())
                .endDate(session.getCurriculumEndDate())
                .trainingDays(session.getCurriculumDays())
                .keyAdjustment(session.getKeyAdjustment())
                .song(songDto)
                .preRecordingFileUrl(null)
                .postRecordingFileUrl(null)
                .curriculum(null)
                .build();
    }

    @Transactional
    public void deleteSession(CustomUserDetails userDetails) {
        // 1) 현재 사용자·모드에 해당하는 세션 조회
        TrainingSession session = trainingSessionRepository.findByUser(userDetails.getUser()).stream()
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
                    .voiceType(song.getVoiceType().name())
                    .pitchNoteMin(song.getPitchNoteMin())
                    .pitchNoteMax(song.getPitchNoteMax());

            // 2-2) 전체 가사
            List<Lyricsline> allLines = lyricslineRepository
                    .findBySongOrderByLineIndex(song);
            List<SongListDTO.LyricLineDTO> lyrics = allLines.stream()
                    .map(l -> SongListDTO.LyricLineDTO.builder()
                            .lineIndex(l.getLineIndex())
                            .text(l.getText())
                            .startTime(l.getStartTimeMs())
                            .build())
                    .collect(Collectors.toList());
            songDTOBuilder.lyrics(lyrics);

            // 2-3) 파일 URL / 앨범 커버
            songDTOBuilder.albumArtUrl(song.getAlbumCoverFile().getFileUrl());
            if (isMr) {
                songDTOBuilder.fileUrl(song.getMrAudioFile().getFileUrl());
            } else {
                songDTOBuilder.fileUrl(song.getOriginalAudioFile().getFileUrl());
            }

            // 2-4) 듀엣 모드인 경우 파트별 인덱스
            if (trainingMode == TrainingMode.DUET) {
                List<DuetSongPart> parts = duetSongPartRepository.findBySong(song);
                List<SongListDTO.DuetPartDTO> duetPartList = parts.stream().map(part -> {
                    List<Integer> idxList = lyricslineRepository
                            .findByDuetSongPart(part).stream()
                            .map(Lyricsline::getLineIndex)
                            .sorted()
                            .collect(Collectors.toList());
                    return SongListDTO.DuetPartDTO.builder()
                            .partNumber(part.getPartNumber())
                            .partName(part.getPartName())
                            .lyricsIndexes(idxList)
                            .build();
                }).collect(Collectors.toList());
                songDTOBuilder.duetParts(duetPartList);
            }

            return songDTOBuilder.build();
        }).collect(Collectors.toList());

        // 3) 리스트 감싸서 반환
        return SongListDTO.builder()
                .songList(songDTOList)
                .build();
    }

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
                .voiceType(song.getVoiceType().name())
                .pitchNoteMin(song.getPitchNoteMin())
                .pitchNoteMax(song.getPitchNoteMax());

        // 3) 전체 가사 조회
        List<Lyricsline> lines = lyricslineRepository.findBySongOrderByLineIndex(song);
        List<SongListDTO.LyricLineDTO> lyrics = lines.stream()
                .map(l -> SongListDTO.LyricLineDTO.builder()
                        .lineIndex(l.getLineIndex())
                        .text(l.getText())
                        .startTime(l.getStartTimeMs())
                        .build())
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
            List<SongListDTO.DuetPartDTO> duetPartList = parts.stream().map(part -> {
                List<Integer> idxs = lyricslineRepository
                        .findByDuetSongPart(part).stream()
                        .map(Lyricsline::getLineIndex)
                        .sorted()
                        .collect(Collectors.toList());
                return SongListDTO.DuetPartDTO.builder()
                        .partNumber(part.getPartNumber())
                        .partName(part.getPartName())
                        .lyricsIndexes(idxs)
                        .build();
            }).collect(Collectors.toList());
            songDTOBuilder.duetParts(duetPartList);
        }

        return songDTOBuilder.build();
    }
}
