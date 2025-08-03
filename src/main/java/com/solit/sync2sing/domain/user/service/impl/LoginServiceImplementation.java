package com.solit.sync2sing.domain.user.service.impl;

import com.solit.sync2sing.domain.user.dto.request.LoginRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LoginResponseDTO;
import com.solit.sync2sing.entity.User;
import com.solit.sync2sing.repository.UserRepository;
import com.solit.sync2sing.domain.user.service.UserLoginService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


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

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseCode.USER_NOT_FOUND.getStatus(),
                        ResponseCode.USER_NOT_FOUND.getMessage()
                ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(
                    ResponseCode.INVALID_PASSWORD.getStatus(),
                    ResponseCode.INVALID_PASSWORD.getMessage()
            );
        }
        List<String> roles = user.getRoles();

        String accessToken = tokenProvider.createAccessToken(username,roles);
        String refreshToken = tokenProvider.createRefreshToken(username);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDTO(accessToken, refreshToken);
    }
}
