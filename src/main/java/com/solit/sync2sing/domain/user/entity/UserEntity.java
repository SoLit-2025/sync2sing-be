package com.solit.sync2sing.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;

@Getter
@Builder
@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String voiceRange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoiceType voiceType;

    @Column(nullable = false)
    private int duetPenaltyCount;

    private LocalDateTime duetPenaltyUntil;
    private String refreshToken;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", nullable = false))
    @Column(name = "role", nullable = false)
    private List<String> roles = new ArrayList<>();

}
