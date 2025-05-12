package com.solit.sync2sing.domain.user.dto.response;

import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SignupResponseDTO {
    private String username; // 사용자의 이메일
    private String nickname;
    private Gender gender;
    private int age;
    private String voiceRange;
    private VoiceType voiceType;
    private int duetPenaltyCount;
    private LocalDateTime duetPenaltyUntil;
}
