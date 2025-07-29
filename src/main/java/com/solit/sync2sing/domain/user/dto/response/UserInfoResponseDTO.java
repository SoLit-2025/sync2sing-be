package com.solit.sync2sing.domain.user.dto.response;

import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserInfoResponseDTO {
    private String username;
    private String nickname;
    private Gender gender;
    private int age;
    private VoiceType voiceType;
    private String pitchNoteMin;
    private String pitchNoteMax;
    private int duetPenaltyCount;
    private LocalDateTime duetPenaltyUntil;
    private int totalTrainingCount;
    private int totalTrainingMinutes;

}
