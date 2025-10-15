package com.solit.sync2sing.domain.user.dto.request;


import com.solit.sync2sing.global.type.Gender;
import com.solit.sync2sing.global.type.VoiceType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDTO {

    private String username;
    private String password;
    private String nickname;
    private Gender gender;
    private int age;
    private String pitchNoteMin;
    private String pitchNoteMax;
    private VoiceType voiceType;
    private Long reportId;
}
