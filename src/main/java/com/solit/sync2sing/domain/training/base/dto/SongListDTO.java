package com.solit.sync2sing.domain.training.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.solit.sync2sing.global.type.VoiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SongListDTO {

    private List<SongDTO> songList;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SongDTO {
        private Long id;
        private String title;
        private String artist;
        private String youtubeLink;

        // 솔로곡 목록 DTO에만 사용되는 필드
        private String voiceType;
        private String pitchNoteMin;
        private String pitchNoteMax;

        private List<LyricLineDTO> lyrics;
        private String albumArtUrl;
        private String fileUrl;

        // 듀엣곡 목록 DTO에만 사용되는 필드
        private String userPartName;
        private List<DuetPartDTO> duetParts;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LyricLineDTO {
        private Integer lineIndex;
        private String text;
        private Integer startTime;

        // 듀엣곡 목록 DTO에만 사용되는 필드
        private Integer partNumber;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DuetPartDTO {
        private Integer partNumber;
        private String partName;
        private String voiceType;
        private String pitchNoteMin;
        private String pitchNoteMax;
    }
}
