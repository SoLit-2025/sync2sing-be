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
    CURRICULUM_CREATED(HttpStatus.CREATED, "커리큘럼 추천 생성에 성공했습니다."),
    SOLO_TRAINING_SESSION_FETCHED(HttpStatus.OK, "솔로 트레이닝 세션 정보 조회에 성공했습니다."),
    SOLO_TRAINING_SESSION_CREATED(HttpStatus.CREATED, "솔로 트레이닝 세션 생성에 성공했습니다."),
    SOLO_TRAINING_SESSION_DELETED(HttpStatus.OK, "솔로 트레이닝 종료 및 관련 데이터(사용자 보컬 오디오 파일 및 진행 중인 훈련 데이터)가 삭제되었습니다."),
    SOLO_MR_SONG_LIST_FETCHED(HttpStatus.OK, "솔로 트레이닝 MR 곡 목록 조회에 성공했습니다."),
    SOLO_ORIGINAL_SONG_LIST_FETCHED(HttpStatus.OK, "솔로 트레이닝 원곡 목록 조회에 성공했습니다."),
    SOLO_MR_SONG_FETCHED(HttpStatus.OK, "솔로 트레이닝 MR 곡 조회에 성공했습니다."),
    SOLO_ORIGINAL_SONG_FETCHED(HttpStatus.OK, "솔로 트레이닝 원곡 조회에 성공했습니다."),
    SOLO_CURRENT_TRAINING_LIST_FETCHED(HttpStatus.OK, "솔로 트레이닝 현재 진행 중인 훈련 목록 조회에 성공했습니다."),
    SOLO_TRAINING_PROGRESS_UPDATED(HttpStatus.OK, "솔로 트레이닝 진행 상황 업데이트에 성공했습니다."),
    VOCAL_ANALYSIS_REPORT_CREATED(HttpStatus.CREATED, "보컬 분석 리포트 생성에 성공했습니다."),

    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입에 성공했습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    SIGNUP_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "이메일, 비밀번호, 닉네임은 필수 항목입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호입니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인에 성공했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_USER_INPUT(HttpStatus.BAD_REQUEST, "요청한 값이 유효하지 않습니다."),
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 서명입니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다."),
    EMPTY_JWT_CLAIMS(HttpStatus.BAD_REQUEST, "JWT 클레임 문자열이 비어 있습니다."),

    ;

    private final HttpStatus status;
    private final String message;
}