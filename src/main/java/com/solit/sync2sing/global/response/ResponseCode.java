package com.solit.sync2sing.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 응답 코드 정의 enum
 *
 * ✅ ResponseCode 네이밍 컨벤션:
 *   형식: RESOURCE_ACTION
 *   예시: CURRICULUM_CREATED, USER_UPDATED, VOCAL_ANALYSIS_COMPLETED
 *
 * 📄 참고: https://www.notion.so/BE-1cef6265368b80a3a8e7ee501dffa0c1?pvs=4
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {

    // 응답 코드 정의
    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입에 성공했습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    SIGNUP_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "이메일, 비밀번호, 닉네임은 필수 항목입니다."),

    ;

    private final HttpStatus status;
    private final String message;
}