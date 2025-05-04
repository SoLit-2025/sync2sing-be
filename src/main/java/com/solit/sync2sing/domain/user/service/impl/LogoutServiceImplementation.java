package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.request.LogoutRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LogoutResponseDTO;
import com.solit.sync2sing.domain.user.entity.UserEntity;
import com.solit.sync2sing.domain.user.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.UserLogoutService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.TokenProvider;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LogoutServiceImplementation implements UserLogoutService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public LogoutResponseDTO logout(String accessToken, LogoutRequestDTO
            logoutRequestDTO) {

        // accessToken 만료 검증
        if (tokenProvider.isTokenExpired(accessToken)) {
            throw new RuntimeException(ResponseCode.EXPIRED_JWT_TOKEN.getMessage());
        }

        String userEmail = tokenProvider.getUsernameFromToken(accessToken);

        UserEntity user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new RuntimeException(ResponseCode.USER_NOT_FOUND.getMessage()));

        // refreshToken 유효성 검사
        String refreshToken = logoutRequestDTO.getRefreshToken();
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(logoutRequestDTO.getRefreshToken())) {
            throw new RuntimeException(ResponseCode.INVALID_JWT_TOKEN.getMessage());
        }

        // 4. AccessToken 블랙리스트 등록
        Long expiration = tokenProvider.getExpirationFromToken(accessToken);
        if (expiration <= 0) {
            throw new RuntimeException(ResponseCode.EXPIRED_JWT_TOKEN.getMessage());
        }
        redisTemplate.opsForValue().set(
                accessToken,
                "blacklisted",
                expiration,
                TimeUnit.MILLISECONDS
        );

        // refreshToken 삭제 (DB에 null로 업데이트)
        user.setRefreshToken(null);
        userRepository.save(user);

        return new LogoutResponseDTO(
                ResponseCode.LOGOUT_SUCCESS.getStatus().value(),
                ResponseCode.LOGOUT_SUCCESS.getMessage()
        );
    }
}
