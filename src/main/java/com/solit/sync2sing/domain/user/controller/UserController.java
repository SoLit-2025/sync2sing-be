package com.solit.sync2sing.domain.user.controller;

import com.solit.sync2sing.domain.user.dto.request.LoginRequestDTO;
import com.solit.sync2sing.domain.user.dto.request.LogoutRequestDTO;
import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LoginResponseDTO;
import com.solit.sync2sing.domain.user.dto.response.LogoutResponseDTO;
import com.solit.sync2sing.domain.user.dto.response.UserInfoResponseDTO;
import com.solit.sync2sing.domain.user.service.UserLoginService;
import com.solit.sync2sing.domain.user.service.UserLogoutService;
import com.solit.sync2sing.domain.user.service.UserSignupService;
import com.solit.sync2sing.domain.user.service.UserInfoService;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserSignupService userSignupService;
    private final UserLoginService userLoginService;
    private final UserLogoutService userLogoutService;
    private final UserInfoService userInfoService;

    public UserController(UserSignupService userSignupService,
                          UserLoginService userLoginService,
                          UserLogoutService userLogoutService,
                          UserInfoService userInfoService) {
        this.userSignupService = userSignupService;
        this.userLoginService = userLoginService;
        this.userLogoutService = userLogoutService;
        this.userInfoService = userInfoService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO> signup(@RequestBody SignupRequestDTO signupRequest) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDTO(
                            ResponseCode.SIGNUP_SUCCESS,
                            userSignupService.signUp(signupRequest)
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(
                            ResponseCode.DUPLICATE_EMAIL
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
                            ResponseCode.INVALID_PASSWORD
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
                            ResponseCode.LOGOUT_SUCCESS
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(
                            ResponseCode.INVALID_REFRESH_TOKEN
                    ));
        }
    }

    @GetMapping
    public ResponseEntity<ResponseDTO> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO(
                            ResponseCode.UNAUTHORIZED
                    ));
        }

        String username = authentication.getName();
        try {
            UserInfoResponseDTO userInfo = userInfoService.getUserInfo(username);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(
                            ResponseCode.USER_INFO_SUCCESS, userInfo
                    ));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(
                            ResponseCode.USER_NOT_FOUND
                    ));
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(
                            ResponseCode.INTERNAL_ERROR
                    ));
        }
    }
}
