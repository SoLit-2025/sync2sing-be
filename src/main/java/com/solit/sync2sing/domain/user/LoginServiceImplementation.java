package com.solit.sync2sing.domain.user;

import com.solit.sync2sing.domain.user.dto.request.LoginRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LoginResponseDTO;
import com.solit.sync2sing.domain.user.entity.UserEntity;
import com.solit.sync2sing.domain.user.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.UserLoginService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImplementation implements UserLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Override
    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        String username = requestDTO.getUsername();
        String password = requestDTO.getPassword();

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(ResponseCode.USER_NOT_FOUND.getMessage()));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException(ResponseCode.INVALID_PASSWORD.getMessage());
        }

        String accessToken = tokenProvider.createAccessToken(username);
        String refreshToken = tokenProvider.createRefreshToken(username);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDTO(accessToken, refreshToken);
    }
}
