package com.solit.sync2sing.domain.user.controller;

import com.solit.sync2sing.domain.user.dto.request.LoginRequestDTO;
import com.solit.sync2sing.domain.user.dto.request.LogoutRequestDTO;
import com.solit.sync2sing.domain.user.dto.request.SignupRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.LoginResponseDTO;
import com.solit.sync2sing.domain.user.dto.response.LogoutResponseDTO;
import com.solit.sync2sing.domain.user.dto.request.UserInfoUpdateRequestDTO;
import com.solit.sync2sing.domain.user.dto.response.UserInfoUpdateResponseDTO;
import com.solit.sync2sing.domain.user.service.UserInfoUpdateService;
import com.solit.sync2sing.domain.user.dto.response.UserInfoResponseDTO;
import com.solit.sync2sing.domain.user.dto.response.SoloVocalAnalysisReportListResponseDTO;
import com.solit.sync2sing.domain.user.service.SoloVocalAnalysisReportListService;
import com.solit.sync2sing.domain.user.service.UserLoginService;
import com.solit.sync2sing.domain.user.service.UserLogoutService;
import com.solit.sync2sing.domain.user.service.UserSignupService;
import com.solit.sync2sing.domain.user.service.UserInfoService;
import com.solit.sync2sing.domain.user.service.impl.SoloVocalAnalysisReportListServiceImpl;
import com.solit.sync2sing.global.response.ResponseCode;
import com.solit.sync2sing.global.response.ResponseDTO;

import com.solit.sync2sing.global.util.SecurityUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.solit.sync2sing.global.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserSignupService userSignupService;
    private final UserLoginService userLoginService;
    private final UserLogoutService userLogoutService;
    private final UserInfoUpdateService userInfoUpdateService;
    private final UserInfoService userInfoService;
    private final SoloVocalAnalysisReportListService soloVocalAnalysisReportListService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserSignupService userSignupService,
                          UserLoginService userLoginService,
                          UserLogoutService userLogoutService,
                          UserInfoUpdateService userInfoUpdateService,
                          UserInfoService userInfoService,
                          SoloVocalAnalysisReportListService soloVocalAnalysisReportListService) {
        this.userSignupService = userSignupService;
        this.userLoginService = userLoginService;
        this.userLogoutService = userLogoutService;
        this.userInfoUpdateService = userInfoUpdateService;
        this.userInfoService = userInfoService;
        this.soloVocalAnalysisReportListService = soloVocalAnalysisReportListService;
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

    @PutMapping
    public ResponseEntity<ResponseDTO> updateUserInfo(@RequestBody UserInfoUpdateRequestDTO requestDTO) {
        try{

            String username = SecurityUtil.getCurrentUser().getUsername();

            UserInfoUpdateResponseDTO responseDTO = userInfoUpdateService.updateUserInfo(username, requestDTO);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(
                            ResponseCode.USER_INFO_UPDATE_SUCCESS,
                            responseDTO
                    ));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(
                            ResponseCode.INVALID_REQUEST
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

    @GetMapping("/reports")
    public ResponseEntity<ResponseDTO<SoloVocalAnalysisReportListResponseDTO>> getSoloReportList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = true) String mode)
    {

        try {
            if(!"solo".equalsIgnoreCase(mode)){
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(ResponseCode.INVALID_REQUEST));
            }
            if(userDetails == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO<>(ResponseCode.UNAUTHORIZED));
            }

            SoloVocalAnalysisReportListResponseDTO responseData =
                    soloVocalAnalysisReportListService.getSoloVocalAnalysisReportList(userDetails);

            return ResponseEntity.ok(
                    new ResponseDTO<>(ResponseCode.SOLO_VOCAL_ANALYSIS_REPORT_LIST_FETCHED, responseData)
            );

        } catch (Exception e) {
            logger.error("API 호출 중 오류 발생 - 코드: {}, 메시지: {}, 예외: {}",
                    ResponseCode.INTERNAL_ERROR.name(),
                    ResponseCode.INTERNAL_ERROR.getMessage(),
                    e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseDTO<>(ResponseCode.INTERNAL_ERROR));
        }
    }



}
