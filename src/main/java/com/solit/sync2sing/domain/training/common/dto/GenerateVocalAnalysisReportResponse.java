package com.solit.sync2sing.domain.training.common.dto;

import com.solit.sync2sing.entity.Song;
import com.solit.sync2sing.global.type.RecordingContext;
import com.solit.sync2sing.global.type.VoiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class GenerateVocalAnalysisReportResponse {

    private Long reportId;
    private RecordingContext analysisType;
    private String title;
    private SongDTO song;
    private int pitchScore;
    private int beatScore;
    private int pronunciationScore;
    private int breathScore;
    private String overallReviewTitle;
    private String overallReviewContent;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SongDTO {

        private Long songId;
        private String title;
        private String artist;
        private VoiceType voiceType;
        private String pitchNoteMin;
        private String pitchNoteMax;
        private String albumCoverUrl;

        public static SongDTO toDTO(Song song) {
            return SongDTO.builder()
                .songId(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .voiceType(song.getVoiceType())
                .pitchNoteMin(song.getPitchNoteMin())
                .pitchNoteMax(song.getPitchNoteMax())
                .albumCoverUrl(song.getAlbumCoverFile().getFileUrl())
                .build();
        }

    }
}
