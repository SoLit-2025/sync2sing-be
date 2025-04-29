package com.solit.sync2sing.domain.user.controller;

import com.solit.sync2sing.domain.user.dto.request.LoginRequestDTO;
import com.solit.sync2sing.domain.user.dto.request.LogoutRequestDTO;
import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LoginResponseDTO;
import com.solit.sync2sing.domain.user.dto.response.LogoutResponseDTO;
import com.solit.sync2sing.domain.user.service.UserLoginService;
import com.solit.sync2sing.domain.user.service.UserLogoutService;
import com.solit.sync2sing.domain.user.service.UserSignupService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserSignupService userSignupService;
    private final UserLoginService userLoginService;
    private final UserLogoutService userLogoutService;

    public UserController(UserSignupService userSignupService,
                          UserLoginService userLoginService,
                          UserLogoutService userLogoutService) {
        this.userSignupService = userSignupService;
        this.userLoginService = userLoginService;
        this.userLogoutService = userLogoutService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO> signup(@RequestBody SignupRequestDTO signupRequest) {
        try {
            userSignupService.signUp(signupRequest);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDTO(
                            ResponseCode.SIGNUP_SUCCESS,
                            null
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(
                            ResponseCode.DUPLICATE_EMAIL,
                            null
                    ));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO loginResponse = userLoginService.login(loginRequest);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(
                            ResponseCode.LOGIN_SUCCESS,
                            loginResponse
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO(
                            ResponseCode.INVALID_PASSWORD,
                            null
                    ));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody LogoutRequestDTO logoutRequestDTO) {

        try {
            // "Bearer {accessToken}" 형식에서 실제 accessToken만 추출
            String accessToken = authorizationHeader.replace("Bearer ", "").trim();

            LogoutResponseDTO logoutResponse = userLogoutService.logout(accessToken, logoutRequestDTO);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(
                            ResponseCode.LOGOUT_SUCCESS,
                            null
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(
                            ResponseCode.INVALID_REFRESH_TOKEN,
                            null
                    ));
        }
    }


}
