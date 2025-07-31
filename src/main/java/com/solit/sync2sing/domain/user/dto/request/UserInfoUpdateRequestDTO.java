package com.solit.sync2sing.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UserInfoUpdateRequestDTO {
    private String nickname;

    public UserInfoUpdateRequestDTO(String nickname){
        this.nickname = nickname;
    }
}
