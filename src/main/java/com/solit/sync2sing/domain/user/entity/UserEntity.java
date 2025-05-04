package com.solit.sync2sing.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private int age;
    private String voiceRange;

    @Enumerated(EnumType.STRING)
    private VoiceType voiceType;

    private int duetPenaltyCount;
    private LocalDateTime duetPenaltyUntil;
}
