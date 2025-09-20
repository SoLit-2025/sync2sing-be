package com.solit.sync2sing.domain.training.duet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuetTrainingRoomListResponse {

    private DuetTrainingRoomDto myRoom;

    private List<DuetTrainingRoomDto> roomList;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuetTrainingRoomDto {
        private Long id;

        private LocalDateTime createdAt;

        private Integer trainingDays;

        private SongDTO song;

        private DuetPartDTO hostPart;

        private DuetPartDTO partnerPart;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SongDTO {

        private Long id;

        private String title;

        private String artist;

        private String albumArtUrl;
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
