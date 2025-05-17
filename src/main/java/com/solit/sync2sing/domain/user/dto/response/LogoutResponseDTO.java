package com.solit.sync2sing.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LogoutResponseDTO {
    private final int status;
    private final String message;
}
