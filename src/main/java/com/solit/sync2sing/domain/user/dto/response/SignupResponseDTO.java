package com.solit.sync2sing.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SignupResponseDTO {
    private String username; // 사용자의 이메일
    private String nickname;
    private String gender;
    private int age;
    private String voiceRange;
    private String voiceType;
    private int duetPenaltyCount;
    private LocalDateTime duetPenaltyUntil;
}
