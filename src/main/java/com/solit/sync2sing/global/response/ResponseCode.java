package com.solit.sync2sing.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

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
    CURRENT_TRAINING_LIST_FETCHED(HttpStatus.OK, "현재 진행 중인 트레이닝 목록 조회에 성공했습니다."),
    CURRICULUM_CREATED(HttpStatus.CREATED, "커리큘럼 추천 생성에 성공했습니다."),
    INVALID_CURRICULUM_DAYS(HttpStatus.BAD_REQUEST, "trainingDays는 3, 7, 14 중 하나여야 합니다."),
    TRAINING_PROGRESS_UPDATED(HttpStatus.OK, "트레이닝 진행 상황 업데이트에 성공했습니다."),
    VOCAL_ANALYSIS_REPORT_CREATED(HttpStatus.CREATED, "보컬 분석 리포트 생성에 성공했습니다."),
    VOCAL_ANALYSIS_REPORT_FETCHED(HttpStatus.OK, "보컬 분석 리포트 상세 조회에 성공했습니다."),
    VOCAL_ANALYSIS_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 보컬 분석 리포트를 찾을 수 없습니다."),
    INVALID_TRAINING_MODE_OR_ANALYSIS_TYPE(HttpStatus.BAD_REQUEST, "트레이닝 모드 또는 분석 타입 요청 값이 유효하지 않습니다."),
    EXTERNAL_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "외부 API 응답 시간 초과. 잠시 후 다시 시도해 주세요."),
    TRANSCRIBE_PARSING_FAILED(HttpStatus.BAD_GATEWAY, "Transcribe 결과 파싱 실패. 다시 시도해 주세요."),
    TRANSCRIPTION_FAIL(HttpStatus.BAD_GATEWAY, "AWS Transcription 처리 중 오류가 발생했습니다."),
    AI_VOICE_ANALYSIS_FAIL(HttpStatus.BAD_GATEWAY, "AI 발성 분석 처리 중 오류가 발생했습니다."),
    RECORDING_NOT_FOUND(HttpStatus.NOT_FOUND, "녹음본을 찾을 수 없습니다."),

    SOLO_TRAINING_SESSION_FETCHED(HttpStatus.OK, "솔로 트레이닝 세션 정보 조회에 성공했습니다."),
    SOLO_TRAINING_SESSION_CREATED(HttpStatus.CREATED, "솔로 트레이닝 세션 생성에 성공했습니다."),
    SOLO_TRAINING_SESSION_DELETED(HttpStatus.OK, "솔로 트레이닝 종료 및 관련 데이터(사용자 보컬 오디오 파일 및 진행 중인 훈련 데이터)가 삭제되었습니다."),
    TRAINING_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 트레이닝 세션을 찾을 수 없습니다."),
    TRAINING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 훈련을 찾을 수 없습니다."),
    INVALID_TRAINING_ID(HttpStatus.BAD_REQUEST, "트레이닝 세션에 해당 훈련이 존재하지 않습니다."),
    SOLO_MR_SONG_LIST_FETCHED(HttpStatus.OK, "솔로 트레이닝 MR 곡 목록 조회에 성공했습니다."),
    SOLO_ORIGINAL_SONG_LIST_FETCHED(HttpStatus.OK, "솔로 트레이닝 원곡 목록 조회에 성공했습니다."),
    SOLO_MR_SONG_FETCHED(HttpStatus.OK, "솔로 트레이닝 MR 곡 조회에 성공했습니다."),
    SOLO_ORIGINAL_SONG_FETCHED(HttpStatus.OK, "솔로 트레이닝 원곡 조회에 성공했습니다."),
    SOLO_VOCAL_ANALYSIS_REPORT_LIST_FETCHED(HttpStatus.OK, "솔로 보컬 분석 리포트 목록 조회에 성공했습니다."),
    
    DUET_AUDIO_MERGED(HttpStatus.CREATED, "듀엣 음원 병합에 성공했습니다."),
    PARTNER_APPLICATION_CREATED(HttpStatus.CREATED, "파트너 신청이 완료되었습니다."),
    RECEIVED_PARTNER_APPLICATIONS_FETCHED(HttpStatus.OK, "받은 파트너 신청 목록 조회에 성공했습니다."),
    SENT_PARTNER_APPLICATIONS_FETCHED(HttpStatus.OK, "보낸 파트너 신청 목록 조회에 성공했습니다."),
    PARTNER_APPLICATION_ACCEPTED(HttpStatus.CREATED, "파트너 신청 수락 및 세션 생성에 성공했습니다."),
    PARTNER_APPLICATION_REJECTED(HttpStatus.OK, "파트너 신청 거절이 완료되었습니다."),
    PARTNER_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "파트너 신청을 찾을 수 없습니다."),
    DUET_TRAINING_ROOMS_FETCHED(HttpStatus.OK, "듀엣 트레이닝 방 목록 조회에 성공했습니다."),
    DUET_TRAINING_ROOM_CREATED(HttpStatus.CREATED, "듀엣 트레이닝 방 생성에 성공했습니다."),
    DUET_TRAINING_ROOM_DELETED(HttpStatus.OK, "듀엣 트레이닝 방 삭제에 성공했습니다."),
    DUET_TRAINING_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "듀엣 트레이닝 방을 찾을 수 없습니다."),
    DUET_TRAINING_ROOM_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 생성한 듀엣 트레이닝 방이 존재합니다. 두 개 이상의 듀엣 트레이닝 방을 생성할 수 없습니다."),
    DUET_TRAINING_ROOM_NOT_PENDING(HttpStatus.CONFLICT, "듀엣 트레이닝 방이 대기(PENDING) 상태가 아닙니다. 현재 상태: %s"),
    DUET_ROOM_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 방에 속한 파트너 신청을 찾을 수 없습니다."),
    DUET_TRAINING_SESSION_FETCHED(HttpStatus.OK, "듀엣 트레이닝 세션 정보 조회에 성공했습니다."),
    DUET_TRAINING_SESSION_DELETED(HttpStatus.OK, "듀엣 트레이닝 종료 및 관련 데이터(사용자 보컬 오디오 파일 및 진행 중인 훈련 데이터)가 삭제되었습니다."),
    DUET_MR_SONG_LIST_FETCHED(HttpStatus.OK, "듀엣 트레이닝 MR 곡 목록 조회에 성공했습니다."),
    DUET_ORIGINAL_SONG_LIST_FETCHED(HttpStatus.OK, "듀엣 트레이닝 원곡 목록 조회에 성공했습니다."),
    DUET_MR_SONG_FETCHED(HttpStatus.OK, "듀엣 트레이닝 MR 곡 조회에 성공했습니다."),
    DUET_ORIGINAL_SONG_FETCHED(HttpStatus.OK, "듀엣 트레이닝 원곡 조회에 성공했습니다."),
    DUET_SONG_PART_NOT_FOUND(HttpStatus.NOT_FOUND, "듀엣곡 파트를 찾을 수 없습니다."),
    DUET_CURRENT_TRAINING_FETCHED(HttpStatus.OK, "현재 진행 중인 듀엣 트레이닝 훈련 목록 조회에 성공했습니다."),
    DUET_CURRENT_TRAINING_EMPTY(HttpStatus.NO_CONTENT, "진행 중인 트레이닝이 없습니다."),
    DUET_TRAINING_PROGRESS_UPDATED(HttpStatus.OK, "듀엣 트레이닝 진행 상황 업데이트에 성공했습니다."),
    AUDIO_MERGE_FAILED(HttpStatus.BAD_GATEWAY, "오디오 파일 병합 실패. 다시 시도해 주세요."),
    DUET_VOCAL_ANALYSIS_REPORT_LIST_FETCHED(HttpStatus.OK, "듀엣 보컬 분석 리포트 목록 조회에 성공했습니다."),

    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입에 성공했습니다."),
    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 아이디입니다."),
    SIGNUP_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "이메일, 비밀번호, 닉네임은 필수 항목입니다."),
    INVALID_USERNAME_FORMAT(HttpStatus.BAD_REQUEST,"아이디는 영문 소문자와 숫자로 6글자 이상 15글자 미만이어야 합니다." ),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호입니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인에 성공했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 예외가 발생했습니다."),
    INVALID_USER_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청 값입니다."),
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 서명입니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다."),
    EMPTY_JWT_CLAIMS(HttpStatus.BAD_REQUEST, "JWT 클레임 문자열이 비어 있습니다."),
    BLACKLISTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "블랙리스트에 등록된 JWT 토큰입니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃에 성공했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 Refresh Token입니다."),
    USER_INFO_UPDATE_SUCCESS(HttpStatus.OK, "회원 정보 수정 성공"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청 형식입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다. 다시 로그인하세요."),
    USER_INFO_SUCCESS(HttpStatus.OK, "회원 정보 조회 성공"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다."),

    ADMIN_SOLOSONG_UPLOADED(HttpStatus.CREATED, "솔로곡 업로드에 성공했습니다."),
    ADMIN_DUETSONG_UPLOADED(HttpStatus.CREATED, "듀엣곡 업로드에 성공했습니다."),
    INVALID_DUET_PART_COUNT(HttpStatus.BAD_REQUEST, "듀엣곡은 파트를 최대 2개까지만 가질 수 있습니다."),
    INVALID_DUET_PART_NUMBER(HttpStatus.BAD_REQUEST, "듀엣곡 part_number는 반드시 0 또는 1이어야 합니다."),
    ADMIN_SONG_DELETED(HttpStatus.OK, "곡 삭제에 성공했습니다."),
    SONG_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 곡을 찾을 수 없습니다."),
    EMPTY_FILE_EXCEPTION(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다."),
    FILE_UPLOAD_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 업로드하는 중 오류가 발생했습니다."),
    FILE_UPLOAD_FAIL_S3_ROLLBACK(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패로 S3 롤백되었습니다."),
    FILE_DELETE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 삭제하는 중 오류가 발생했습니다."),


    ;

    private final HttpStatus status;
    private final String message;

    // 요청한 HttpStatus, 메시지에 맞는 enum 상수를 찾아 반환
    public static ResponseCode from(HttpStatusCode statusCode, String message) {
        for (ResponseCode code : ResponseCode.values()) {
            if (code.getStatus().value() == statusCode.value() && code.getMessage().equals(message)) {
                return code;
            }
        }
        return INTERNAL_ERROR; // 기본값으로 INTERNAL_ERROR 반환
    }
}