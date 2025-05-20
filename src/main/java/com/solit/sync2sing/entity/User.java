package com.solit.sync2sing.entity;

import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "username", nullable = false)
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 255)
    @NotNull
    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @NotNull
    @Column(name = "age", nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "voice_type", nullable = false, length = 50)
    private VoiceType voiceType;

    @Size(max = 255)
    @NotNull
    @Column(name = "voice_range", nullable = false)
    private String voiceRange;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "duet_penalty_count", nullable = false)
    private Integer duetPenaltyCount;

    @Column(name = "duet_penalty_until")
    private LocalDateTime duetPenaltyUntil;

    @Size(max = 512)
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", nullable = false))
    @Column(name = "role", nullable = false)
    private List<String> roles = new ArrayList<>();

}