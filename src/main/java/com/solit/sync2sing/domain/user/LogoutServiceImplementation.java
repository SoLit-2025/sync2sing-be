package com.solit.sync2sing.domain.user;

import com.solit.sync2sing.domain.user.dto.request.LogoutRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LogoutResponseDTO;
import com.solit.sync2sing.domain.user.entity.UserEntity;
import com.solit.sync2sing.domain.user.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.UserLogoutService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutServiceImplementation implements UserLogoutService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Override
    public LogoutResponseDTO logout(String accessToken, LogoutRequestDTO logoutRequestDTO) {

        String userEmail = tokenProvider.getUsernameFromToken(accessToken);

        UserEntity user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new RuntimeException(ResponseCode.USER_NOT_FOUND.getMessage()));

        if (!user.getRefreshToken().equals(logoutRequestDTO.getRefreshToken())) {
            throw new RuntimeException(ResponseCode.INVALID_JWT_TOKEN.getMessage());
        }

        // refreshToken 삭제 (DB에 null로 업데이트)
        user.setRefreshToken(null);
        userRepository.save(user);

        return new LogoutResponseDTO(
                ResponseCode.LOGOUT_SUCCESS.getStatus().value(),
                ResponseCode.LOGOUT_SUCCESS.getMessage()
        );
    }
}
