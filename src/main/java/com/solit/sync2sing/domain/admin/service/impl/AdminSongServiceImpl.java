package com.solit.sync2sing.domain.admin.service.impl;

import com.solit.sync2sing.domain.admin.dto.AdminDuetSongUploadRequest;
import com.solit.sync2sing.domain.admin.dto.AdminSongDeleteRequest;
import com.solit.sync2sing.domain.admin.dto.AdminSoloSongUploadRequest;
import com.solit.sync2sing.domain.admin.service.AdminSongService;
import com.solit.sync2sing.entity.*;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.global.type.VoiceType;
import com.solit.sync2sing.global.util.S3Util;
import com.solit.sync2sing.repository.DuetSongPartRepository;
import com.solit.sync2sing.repository.LyricslineRepository;
import com.solit.sync2sing.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSongServiceImpl implements AdminSongService {

    private final S3Util s3Util;
    private final SongRepository songRepository;
    private final LyricslineRepository lyricslineRepository;
    private final DuetSongPartRepository duetSongPartRepository;

    @Override
    public void adminSoloSongUpload(
            MultipartFile albumCoverImage,
            MultipartFile originalAudio,
            MultipartFile mrAudio,
            AdminSoloSongUploadRequest request
    ) {
        String albumCoverS3Url = null;
        String originalAudioS3Url = null;
        String mrAudioS3Url = null;

        try {
            albumCoverS3Url = s3Util.saveAlbumCoverToS3(albumCoverImage);
            ImageFile albumCoverImageFile = ImageFile.builder()
                    .fileName(albumCoverImage.getOriginalFilename())
                    .fileUrl(albumCoverS3Url)
                    .build();

            originalAudioS3Url = s3Util.saveOriginalAudioToS3(originalAudio);
            AudioFile originalAudioFile = AudioFile.builder()
                    .fileName(originalAudio.getOriginalFilename())
                    .fileUrl(originalAudioS3Url)
                    .build();

            mrAudioS3Url = s3Util.saveMrAudioToS3(mrAudio);
            AudioFile mrAudioFile = AudioFile.builder()
                    .fileName(mrAudio.getOriginalFilename())
                    .fileUrl(mrAudioS3Url)
                    .build();

            Song song = Song.builder()
                    .albumCoverFile(albumCoverImageFile)
                    .originalAudioFile(originalAudioFile)
                    .mrAudioFile(mrAudioFile)
                    .trainingMode(TrainingMode.valueOf(request.getTrainingMode()))
                    .title(request.getTitle())
                    .artist(request.getArtist())
                    .voiceType(VoiceType.valueOf(request.getVoiceType()))
                    .pitchNoteMin(request.getPitchNoteMin())
                    .pitchNoteMax(request.getPitchNoteMax())
                    .build();
            songRepository.save(song);

            List<Lyricsline> lines = request.getLyrics().stream()
                    .map(dto -> Lyricsline.builder()
                            .song(song)
                            .lineIndex(dto.getLineIndex())
                            .text(dto.getText())
                            .startTimeMs(dto.getStartTime())
                            .build())
                    .collect(Collectors.toList());
            lyricslineRepository.saveAll(lines);

        } catch (Exception e) {
            if (albumCoverS3Url != null) s3Util.deleteFileFromS3(albumCoverS3Url);
            if (originalAudioS3Url != null) s3Util.deleteFileFromS3(originalAudioS3Url);
            if (mrAudioS3Url != null) s3Util.deleteFileFromS3(mrAudioS3Url);

            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getStatus(),
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getMessage()
            );
        }
    }

    @Override
    public void adminDuetSongUpload(
            MultipartFile albumCoverImage,
            MultipartFile originalAudio,
            MultipartFile mrAudio,
            AdminDuetSongUploadRequest request
    ) {
        String albumCoverS3Url = null;
        String originalAudioS3Url = null;
        String mrAudioS3Url = null;

        try {
            albumCoverS3Url = s3Util.saveAlbumCoverToS3(albumCoverImage);
            ImageFile albumCoverImageFile = ImageFile.builder()
                    .fileName(albumCoverImage.getOriginalFilename())
                    .fileUrl(albumCoverS3Url)
                    .build();

            originalAudioS3Url = s3Util.saveOriginalAudioToS3(originalAudio);
            AudioFile originalAudioFile = AudioFile.builder()
                    .fileName(originalAudio.getOriginalFilename())
                    .fileUrl(originalAudioS3Url)
                    .build();

            mrAudioS3Url = s3Util.saveMrAudioToS3(mrAudio);
            AudioFile mrAudioFile = AudioFile.builder()
                    .fileName(mrAudio.getOriginalFilename())
                    .fileUrl(mrAudioS3Url)
                    .build();

            Song song = Song.builder()
                    .albumCoverFile(albumCoverImageFile)
                    .originalAudioFile(originalAudioFile)
                    .mrAudioFile(mrAudioFile)
                    .trainingMode(TrainingMode.DUET)
                    .title(request.getTitle())
                    .artist(request.getArtist())
                    .voiceType(VoiceType.valueOf(request.getVoiceType()))
                    .pitchNoteMin(request.getPitchNoteMin())
                    .pitchNoteMax(request.getPitchNoteMax())
                    .build();
            songRepository.save(song);

            LinkedHashMap<String, Integer> partOrder = new LinkedHashMap<>();
            int nextNumber = 0;
            for (AdminDuetSongUploadRequest.LyricLineDTO dto : request.getLyrics()) {
                String partName = dto.getPartName();

                if (!partOrder.containsKey(partName)) {
                    partOrder.put(partName, nextNumber++);
                    if (partOrder.size() > 2) {
                        throw new ResponseStatusException(
                                ResponseCode.INVALID_DUET_PART_COUNT.getStatus(),
                                ResponseCode.INVALID_DUET_PART_COUNT.getMessage()
                        );
                    }
                }
            }

            List<DuetSongPart> parts = partOrder.entrySet().stream()
                    .map(e -> DuetSongPart.builder()
                            .song(song)
                            .partNumber(e.getValue())
                            .partName(e.getKey())
                            .build())
                    .collect(Collectors.toList());
            duetSongPartRepository.saveAll(parts);

            Map<String, DuetSongPart> partByName = parts.stream()
                    .collect(Collectors.toMap(
                            DuetSongPart::getPartName,
                            p -> p
                    ));

            List<Lyricsline> lines = request.getLyrics().stream()
                    .map(dto -> Lyricsline.builder()
                            .song(song)
                            .duetSongPart(partByName.get(dto.getPartName()))
                            .lineIndex(dto.getLineIndex())
                            .text(dto.getText())
                            .startTimeMs(dto.getStartTime())
                            .build())
                    .collect(Collectors.toList());
            lyricslineRepository.saveAll(lines);

        } catch (Exception e) {
            if (albumCoverS3Url != null) s3Util.deleteFileFromS3(albumCoverS3Url);
            if (originalAudioS3Url != null) s3Util.deleteFileFromS3(originalAudioS3Url);
            if (mrAudioS3Url != null) s3Util.deleteFileFromS3(mrAudioS3Url);

            throw new ResponseStatusException(
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getStatus(),
                    ResponseCode.FILE_UPLOAD_FAIL_S3_ROLLBACK.getMessage()
            );
        }
    }


    @Override
    public void adminSongDelete(AdminSongDeleteRequest request) {
        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.SONG_NOT_FOUND.getStatus(),
                        ResponseCode.SONG_NOT_FOUND.getMessage()
                ));

        s3Util.deleteFileFromS3(song.getOriginalAudioFile().getFileUrl());
        s3Util.deleteFileFromS3(song.getMrAudioFile().getFileUrl());
        s3Util.deleteFileFromS3(song.getAlbumCoverFile().getFileUrl());

        songRepository.delete(song);
    }
}
