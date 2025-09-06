package com.solit.sync2sing.domain.training.base.service;

import com.solit.sync2sing.domain.training.base.dto.SessionDTO;
import com.solit.sync2sing.domain.training.base.dto.SongListDTO;
import com.solit.sync2sing.domain.training.common.dto.CurriculumListResponse;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.CustomUserDetails;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.SessionStatus;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.global.type.VoiceType;
import com.solit.sync2sing.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbstractTrainingServiceTest {

    @Mock
    TrainingSessionRepository trainingSessionRepository;
    @Mock
    TrainingSessionTrainingRepository trainingSessionTrainingRepository;
    @Mock
    RecordingRepository recordingRepository;
    @Mock
    DuetTrainingRoomRepository duetTrainingRoomRepository;
    @Mock
    SongRepository songRepository;
    @Mock
    LyricslineRepository lyricslineRepository;
    @Mock
    DuetSongPartRepository duetSongPartRepository;

    // 두 모드를 각각 테스트하기 위해 익명 서브클래스 생성
    private AbstractTrainingService soloService;
    private AbstractTrainingService duetService;

    @BeforeEach
    void setUp() {
        soloService = new AbstractTrainingService(
                TrainingMode.SOLO,
                trainingSessionRepository,
                trainingSessionTrainingRepository,
                recordingRepository,
                duetTrainingRoomRepository,
                songRepository,
                lyricslineRepository,
                duetSongPartRepository
        ) {};
        duetService = new AbstractTrainingService(
                TrainingMode.DUET,
                trainingSessionRepository,
                trainingSessionTrainingRepository,
                recordingRepository,
                duetTrainingRoomRepository,
                songRepository,
                lyricslineRepository,
                duetSongPartRepository
        ) {};
    }

    @Test
    void testGetSession_SoloMode() {
        // --- 준비 (given) ---
        User user = new User();  // 엔티티 클래스 User 기본 생성자 사용 가정
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(user);

        // 1) SOLO 세션
        TrainingSession session = mock(TrainingSession.class);
        when(session.getTrainingMode()).thenReturn(TrainingMode.SOLO);
        when(session.getStatus()).thenReturn(SessionStatus.TRAINING_IN_PROGRESS);
        when(session.getCurriculumStartDate()).thenReturn(LocalDateTime.of(2025,4,1,0,0));
        when(session.getCurriculumEndDate())  .thenReturn(LocalDateTime.of(2025,4,7,0,0));
        when(session.getCurriculumDays())     .thenReturn(7);
        when(session.getKeyAdjustment())      .thenReturn(-2);

        // 2) Song
        Song song = mock(Song.class);
        when(song.getId()).thenReturn(1L);
        when(song.getTitle()).thenReturn("Shape of You");
        when(song.getArtist()).thenReturn("Ed Sheeran");
        when(session.getSong()).thenReturn(song);

        // 3) 녹음
        Recording preRec = mock(Recording.class);
        AudioFile preFile = mock(AudioFile.class);
        when(preFile.getFileUrl()).thenReturn("pre-url");
        when(preRec.getAudioFile()).thenReturn(preFile);
        when(recordingRepository.findByTrainingSessionAndRecordingPhase(session, RecordingContext.PRE))
                .thenReturn(Optional.of(preRec));
        when(recordingRepository.findByTrainingSessionAndRecordingPhase(session, RecordingContext.POST))
                .thenReturn(Optional.empty());

        // 4) 커리큘럼 (빈 리스트)
        when(trainingSessionTrainingRepository.findByTrainingSession(session))
                .thenReturn(Collections.emptyList());

        // 5) 세션 리포지토리
        when(trainingSessionRepository.findByUserId(user.getId()))
                .thenReturn(List.of(session));

        // --- 실행 (when) ---
        SessionDTO dto = soloService.getSession(userDetails).get();

        // --- 검증 (then) ---
        assertEquals(SessionStatus.TRAINING_IN_PROGRESS, dto.getStatus());
        assertEquals(LocalDateTime.of(2025,4,1,0,0), dto.getStartDate());
        assertEquals(LocalDateTime.of(2025,4,7,0,0), dto.getEndDate());
        assertEquals(7, dto.getTrainingDays());
        assertEquals(-2, dto.getKeyAdjustment());

        // SongDTO 검증
        SongListDTO.SongDTO sd = dto.getSong();
        assertEquals(1L, sd.getId());
        assertEquals("Shape of You", sd.getTitle());
        assertEquals("Ed Sheeran", sd.getArtist());
        assertNull(sd.getUserPartName(), "SOLO 모드에는 userPartName이 없어야 합니다");

        // Recording 검증
        assertEquals("pre-url", dto.getPreRecordingFileUrl());
        assertNull(dto.getPostRecordingFileUrl(), "POST 녹음이 없을 때는 null");

        // Curriculum 검증: 모두 빈 리스트
        CurriculumListResponse cr = dto.getCurriculum();
        assertTrue(cr.getPitch().isEmpty());
        assertTrue(cr.getRhythm().isEmpty());
        assertTrue(cr.getPronunciation().isEmpty());
        assertTrue(cr.getBreath().isEmpty());
    }

    @Test
    void testGetSession_DuetMode() {
        // --- 준비 (given) ---
        User user = new User();
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(user);

        // 1) DUET 세션
        TrainingSession session = mock(TrainingSession.class);
        when(session.getTrainingMode()).thenReturn(TrainingMode.DUET);
        when(session.getStatus()).thenReturn(SessionStatus.TRAINING_IN_PROGRESS);
        when(session.getCurriculumStartDate()).thenReturn(LocalDateTime.of(2025,4,1,0,0));
        when(session.getCurriculumEndDate())  .thenReturn(LocalDateTime.of(2025,4,7,0,0));
        when(session.getCurriculumDays())     .thenReturn(7);
        when(session.getKeyAdjustment())      .thenReturn(-2);
        when(session.getSong()).thenReturn(mock(Song.class)); // song 정보는 바로 덮어씌움

        // 2) 룸
        DuetTrainingRoom room = mock(DuetTrainingRoom.class);
        when(duetTrainingRoomRepository
                .findByHostTrainingSessionOrPartnerTrainingSession(session, session))
                .thenReturn(Optional.of(room));

        // 파트(Host) 및 마감일
        DuetSongPart part = mock(DuetSongPart.class);
        when(room.getHostTrainingSession()).thenReturn(session);
        when(room.getHostUserPart()).thenReturn(part);
        when(part.getPartName()).thenReturn("영희");
        when(room.getPreRecordingDueDate())
                .thenReturn(LocalDateTime.of(2025,3,30,12,0));
        when(room.getPostRecordingDueDate())
                .thenReturn(LocalDateTime.of(2025,4,10,18,30));

        // 3) Song 상세
        Song song = mock(Song.class);
        when(song.getId()).thenReturn(1L);
        when(song.getTitle()).thenReturn("Shape of You");
        when(song.getArtist()).thenReturn("Ed Sheeran");
        when(session.getSong()).thenReturn(song);

        // 4) 녹음
        Recording preRec = mock(Recording.class);
        AudioFile preFile = mock(AudioFile.class);
        when(preFile.getFileUrl()).thenReturn("pre-url");
        when(preRec.getAudioFile()).thenReturn(preFile);
        when(recordingRepository.findByTrainingSessionAndRecordingPhase(session, RecordingContext.PRE))
                .thenReturn(Optional.of(preRec));
        when(recordingRepository.findByTrainingSessionAndRecordingPhase(session, RecordingContext.POST))
                .thenReturn(Optional.empty());

        // 5) 커리큘럼
        when(trainingSessionTrainingRepository.findByTrainingSession(session))
                .thenReturn(Collections.emptyList());

        // 6) 세션 리포지토리
        when(trainingSessionRepository.findByUserId(user.getId()))
                .thenReturn(List.of(session));

        // --- 실행 (when) ---
        SessionDTO dto = duetService.getSession(userDetails).get();

        // --- 검증 (then) ---
        // 공통 검증
        assertEquals(SessionStatus.TRAINING_IN_PROGRESS, dto.getStatus());
        assertEquals(1L, dto.getSong().getId());
        assertEquals("Shape of You", dto.getSong().getTitle());
        assertEquals("Ed Sheeran", dto.getSong().getArtist());

        // DUET 전용: userPartName, dueDate
        assertEquals("영희", dto.getSong().getUserPartName());
        assertEquals(LocalDateTime.of(2025,3,30,12,0), dto.getPreRecordingDueDate());
        assertEquals(LocalDateTime.of(2025,4,10,18,30), dto.getPostRecordingDueDate());
    }

    @Test
    void testGetSongList_InvalidType() {
        assertThrows(ResponseStatusException.class, () -> soloService.getSongList("foo"),
                "잘못된 type 값이면 예외 발생해야 함");
    }

    @Test
    void testGetSongList_SoloMode() {
        // given
        Song song = mock(Song.class);
        when(song.getId()).thenReturn(1L);
        when(song.getTitle()).thenReturn("Shape of You");
        when(song.getArtist()).thenReturn("Ed Sheeran");
        when(song.getVoiceType()).thenReturn(VoiceType.TENOR);
        when(song.getPitchNoteMin()).thenReturn("E1");
        when(song.getPitchNoteMax()).thenReturn("F4");

        AudioFile original = mock(AudioFile.class);
        when(original.getFileUrl()).thenReturn("orig-url");
        ImageFile album = mock(ImageFile.class);
        when(album.getFileUrl()).thenReturn("album-url");
        when(song.getOriginalAudioFile()).thenReturn(original);
        when(song.getAlbumCoverFile()).thenReturn(album);

        Lyricsline line1 = mock(Lyricsline.class);
        when(line1.getLineIndex()).thenReturn(0);
        when(line1.getText()).thenReturn("L0");
        when(line1.getStartTimeMs()).thenReturn(0);
        Lyricsline line2 = mock(Lyricsline.class);
        when(line2.getLineIndex()).thenReturn(1);
        when(line2.getText()).thenReturn("L1");
        when(line2.getStartTimeMs()).thenReturn(5000);
        when(lyricslineRepository.findBySongOrderByLineIndex(song))
                .thenReturn(List.of(line1, line2));

        when(songRepository.findByTrainingMode(TrainingMode.SOLO))
                .thenReturn(List.of(song));

        // when
        SongListDTO dto = soloService.getSongList("original");

        // then
        assertNotNull(dto);
        assertEquals(1, dto.getSongList().size());
        SongListDTO.SongDTO dto1 = dto.getSongList().get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Shape of You", dto1.getTitle());
        assertEquals("Ed Sheeran", dto1.getArtist());
        assertEquals("TENOR", dto1.getVoiceType());
        assertEquals("E1", dto1.getPitchNoteMin());
        assertEquals("F4", dto1.getPitchNoteMax());
        assertEquals("orig-url", dto1.getFileUrl());
        assertEquals("album-url", dto1.getAlbumArtUrl());
        assertNotNull(dto1.getLyrics());
        assertEquals(2, dto1.getLyrics().size());
        assertNull(dto1.getDuetParts(), "SOLO 모드엔 duetParts 없어야 함");
    }

    @Test
    void testGetSongList_DuetMode() {
        // given
        Song song = mock(Song.class);
        when(song.getId()).thenReturn(2L);
        when(song.getTitle()).thenReturn("Duet Song");
        when(song.getArtist()).thenReturn("Artist");
        when(song.getVoiceType()).thenReturn(VoiceType.ALTO);
        when(song.getPitchNoteMin()).thenReturn("D3");
        when(song.getPitchNoteMax()).thenReturn("E5");

        AudioFile original = mock(AudioFile.class);
        when(original.getFileUrl()).thenReturn("orig-duet");
        ImageFile album = mock(ImageFile.class);
        when(album.getFileUrl()).thenReturn("album-duet");
        when(song.getOriginalAudioFile()).thenReturn(original);
        when(song.getAlbumCoverFile()).thenReturn(album);

        Lyricsline lineA = mock(Lyricsline.class);
        when(lineA.getLineIndex()).thenReturn(0);
        when(lineA.getText()).thenReturn("LA");
        when(lineA.getStartTimeMs()).thenReturn(0);
        Lyricsline lineB = mock(Lyricsline.class);
        when(lineB.getLineIndex()).thenReturn(1);
        when(lineB.getText()).thenReturn("LB");
        when(lineB.getStartTimeMs()).thenReturn(4000);
        when(lyricslineRepository.findBySongOrderByLineIndex(song))
                .thenReturn(List.of(lineA, lineB));

        DuetSongPart part1 = mock(DuetSongPart.class);
        when(part1.getPartNumber()).thenReturn(1);
        when(part1.getPartName()).thenReturn("A");
        DuetSongPart part2 = mock(DuetSongPart.class);
        when(part2.getPartNumber()).thenReturn(2);
        when(part2.getPartName()).thenReturn("B");
        when(duetSongPartRepository.findBySong(song))
                .thenReturn(List.of(part1, part2));
        when(lyricslineRepository.findByDuetSongPart(part1))
                .thenReturn(List.of(lineA));
        when(lyricslineRepository.findByDuetSongPart(part2))
                .thenReturn(List.of(lineB));

        when(songRepository.findByTrainingMode(TrainingMode.DUET))
                .thenReturn(List.of(song));

        // when
        SongListDTO dto = duetService.getSongList("original");

        // then
        assertNotNull(dto);
        assertEquals(1, dto.getSongList().size());
        SongListDTO.SongDTO dto2 = dto.getSongList().get(0);
        assertEquals(2L, dto2.getId());
        assertEquals(2, dto2.getLyrics().size());
        assertNotNull(dto2.getDuetParts(), "DUET 모드엔 duetParts 있어야 함");
        assertEquals(2, dto2.getDuetParts().size());
        assertEquals(List.of(0), dto2.getDuetParts().get(0).getLyricsIndexes());
        assertEquals(List.of(1), dto2.getDuetParts().get(1).getLyricsIndexes());
    }

    @Test
    void testGetSong_InvalidType() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> soloService.getSong(1L, "invalid")
        );
        assertEquals(
                ResponseCode.INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE.getMessage(),
                ex.getReason()
        );
    }

    @Test
    void testGetSong_SoloOriginal() {
        // given
        Song song = mock(Song.class);
        when(songRepository.findById(1L)).thenReturn(java.util.Optional.of(song));
        when(song.getId()).thenReturn(1L);
        when(song.getTitle()).thenReturn("Shape of You");
        when(song.getArtist()).thenReturn("Ed Sheeran");
        when(song.getVoiceType()).thenReturn(com.solit.sync2sing.global.type.VoiceType.TENOR);
        when(song.getPitchNoteMin()).thenReturn("E1");
        when(song.getPitchNoteMax()).thenReturn("F4");

        AudioFile orig = mock(AudioFile.class);
        when(orig.getFileUrl()).thenReturn("orig-url");
        when(song.getOriginalAudioFile()).thenReturn(orig);

        ImageFile album = mock(ImageFile.class);
        when(album.getFileUrl()).thenReturn("album-solo");
        when(song.getOriginalAudioFile()).thenReturn(orig);
        when(song.getAlbumCoverFile()).thenReturn(album);

        Lyricsline l1 = mock(Lyricsline.class);
        when(l1.getLineIndex()).thenReturn(0);
        when(l1.getText()).thenReturn("L0");
        when(l1.getStartTimeMs()).thenReturn(0);
        when(lyricslineRepository.findBySongOrderByLineIndex(song))
                .thenReturn(List.of(l1));

        // when
        SongListDTO.SongDTO dto = soloService.getSong(1L, "original");

        // then
        assertEquals(1L, dto.getId());
        assertEquals("Shape of You", dto.getTitle());
        assertEquals("Ed Sheeran", dto.getArtist());
        assertEquals("TENOR", dto.getVoiceType());
        assertEquals("E1", dto.getPitchNoteMin());
        assertEquals("F4", dto.getPitchNoteMax());
        assertEquals(List.of(0), dto.getLyrics().stream().map(l -> l.getLineIndex()).toList());
        assertEquals("orig-url", dto.getFileUrl());
        assertEquals("album-solo", dto.getAlbumArtUrl());
        assertNull(dto.getDuetParts());
    }

    @Test
    void testGetSong_SoloMr() {
        // setup as solo original but change type
        Song song = mock(Song.class);
        when(songRepository.findById(2L)).thenReturn(java.util.Optional.of(song));
        when(song.getId()).thenReturn(2L);
        when(song.getTitle()).thenReturn("Test");
        when(song.getArtist()).thenReturn("Artist");
        when(song.getVoiceType()).thenReturn(com.solit.sync2sing.global.type.VoiceType.ALTO);
        when(song.getPitchNoteMin()).thenReturn("A2");
        when(song.getPitchNoteMax()).thenReturn("C5");

        AudioFile mr = mock(AudioFile.class);
        when(mr.getFileUrl()).thenReturn("mr-url");
        when(song.getMrAudioFile()).thenReturn(mr);

        ImageFile album = mock(ImageFile.class);
        when(album.getFileUrl()).thenReturn("album-solo");
        when(song.getMrAudioFile()).thenReturn(mr);
        when(song.getAlbumCoverFile()).thenReturn(album);

        Lyricsline l = mock(Lyricsline.class);
        when(l.getLineIndex()).thenReturn(1);
        when(l.getText()).thenReturn("L1");
        when(l.getStartTimeMs()).thenReturn(1000);
        when(lyricslineRepository.findBySongOrderByLineIndex(song))
                .thenReturn(List.of(l));

        SongListDTO.SongDTO dto = soloService.getSong(2L, "mr");
        assertEquals("mr-url", dto.getFileUrl());
        assertEquals(List.of(1),
                dto.getLyrics().stream()
                        .map(SongListDTO.LyricLineDTO::getLineIndex)
                        .toList()
        );
    }

    @Test
    void testGetSong_DuetOriginal() {
        // given
        Song song = mock(Song.class);
        when(songRepository.findById(3L)).thenReturn(java.util.Optional.of(song));
        when(song.getId()).thenReturn(3L);
        when(song.getTitle()).thenReturn("Duet");
        when(song.getArtist()).thenReturn("Singer");
        when(song.getVoiceType()).thenReturn(com.solit.sync2sing.global.type.VoiceType.SOPRANO);
        when(song.getPitchNoteMin()).thenReturn("B2");
        when(song.getPitchNoteMax()).thenReturn("D5");

        AudioFile orig2 = mock(AudioFile.class);
        when(orig2.getFileUrl()).thenReturn("orig2-url");
        ImageFile album = mock(ImageFile.class);
        when(album.getFileUrl()).thenReturn("album-url");
        when(song.getOriginalAudioFile()).thenReturn(orig2);
        when(song.getAlbumCoverFile()).thenReturn(album);

        Lyricsline lx = mock(Lyricsline.class);
        when(lx.getLineIndex()).thenReturn(2);
        when(lx.getText()).thenReturn("LX");
        when(lx.getStartTimeMs()).thenReturn(2000);
        when(lyricslineRepository.findBySongOrderByLineIndex(song))
                .thenReturn(List.of(lx));

        DuetSongPart p1 = mock(DuetSongPart.class);
        when(p1.getPartNumber()).thenReturn(1);
        when(p1.getPartName()).thenReturn("A");
        DuetSongPart p2 = mock(DuetSongPart.class);
        when(p2.getPartNumber()).thenReturn(2);
        when(p2.getPartName()).thenReturn("B");
        when(duetSongPartRepository.findBySong(song))
                .thenReturn(List.of(p1, p2));
        when(lyricslineRepository.findByDuetSongPart(p1))
                .thenReturn(List.of(lx));
        when(lyricslineRepository.findByDuetSongPart(p2))
                .thenReturn(List.of());

        // when
        SongListDTO.SongDTO dto = duetService.getSong(3L, "original");

        // then
        assertEquals(3L, dto.getId());
        assertEquals("orig2-url", dto.getFileUrl());
        assertEquals("album-url", dto.getAlbumArtUrl());
        assertNotNull(dto.getDuetParts());
        assertEquals(1, dto.getDuetParts().get(0).getLyricsIndexes().size());
    }

    @Test
    void testGetSong_DuetMr() {
        // similar to original but type=mr
        Song song = mock(Song.class);
        when(songRepository.findById(4L)).thenReturn(java.util.Optional.of(song));
        AudioFile mr2 = mock(AudioFile.class);
        when(mr2.getFileUrl()).thenReturn("mr2-url");
        when(song.getMrAudioFile()).thenReturn(mr2);
        ImageFile alb2 = mock(ImageFile.class);
        when(alb2.getFileUrl()).thenReturn("alb2-url");
        when(song.getAlbumCoverFile()).thenReturn(alb2);
        when(lyricslineRepository.findBySongOrderByLineIndex(song))
                .thenReturn(List.of());
        when(duetSongPartRepository.findBySong(song)).thenReturn(List.of());
        when(song.getVoiceType()).thenReturn(com.solit.sync2sing.global.type.VoiceType.SOPRANO);


        SongListDTO.SongDTO dto = duetService.getSong(4L, "mr");
        assertEquals("mr2-url", dto.getFileUrl());
        assertEquals("alb2-url", dto.getAlbumArtUrl());
    }
}
