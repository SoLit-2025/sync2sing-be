package com.solit.sync2sing.domain.user.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private String username;
    private String password;
    private String nickname;
    private String gender;
    private int age;
    private String voiceRange;
    private String voiceType;
    private int duetPenaltyCount;
    private LocalDateTime duetPenaltyUntil;
}
