package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.request.LogoutRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LogoutResponseDTO;
import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.UserLogoutService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.TokenProvider;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LogoutServiceImplementation implements UserLogoutService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public LogoutResponseDTO logout(String accessToken, LogoutRequestDTO
            logoutRequestDTO) {

        // accessToken 만료 검증
        if (tokenProvider.isTokenExpired(accessToken)) {
            throw new ResponseStatusException(
                    ResponseCode.EXPIRED_JWT_TOKEN.getStatus(),
                    ResponseCode.EXPIRED_JWT_TOKEN.getMessage()
            );
        }

        String userEmail = tokenProvider.getUsernameFromToken(accessToken);

        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.USER_NOT_FOUND.getStatus(),
                        ResponseCode.USER_NOT_FOUND.getMessage()
                ));

        // refreshToken 유효성 검사
        String refreshToken = logoutRequestDTO.getRefreshToken();
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(logoutRequestDTO.getRefreshToken())) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_REFRESH_TOKEN.getStatus(),
                    ResponseCode.INVALID_REFRESH_TOKEN.getMessage()
            );
        }

        // AccessToken 블랙리스트 등록
        Long expiration = tokenProvider.getExpirationFromToken(accessToken);
        if (expiration <= 0) {
            throw new ResponseStatusException(
                    ResponseCode.EXPIRED_JWT_TOKEN.getStatus(),
                    ResponseCode.EXPIRED_JWT_TOKEN.getMessage()
            );
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
