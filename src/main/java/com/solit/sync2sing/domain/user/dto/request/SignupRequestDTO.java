package com.solit.sync2sing.domain.user.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDTO {

    private String username;
    private String password;
    private String nickname;
    private String gender;
    private int age;
    private String voiceRange;
    private String voiceType;

}
