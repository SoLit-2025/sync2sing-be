package com.solit.sync2sing.domain.user.dto.response;

import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;
import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserInfoUpdateResponseDTO {
    private String username;
    private String nickname;
    private Gender gender;
    private Integer age;
    private VoiceType voiceType;
    private String voiceRange;
    private String pitchNoteMin;
    private String pitchNoteMax;
    private int duetPenaltyCount;
    private LocalDateTime duetPenaltyUntil;
    private int totalTrainingMinutes;
    private int totalTrainingCount;

}
