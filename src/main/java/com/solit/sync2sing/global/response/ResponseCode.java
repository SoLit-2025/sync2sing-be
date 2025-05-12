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
    CURRENT_TRAINING_LIST_FETCHED(HttpStatus.OK, "현재 진행 중인 트레이닝 목록 조회에 성공했습니다."),
    CURRICULUM_CREATED(HttpStatus.CREATED, "커리큘럼 추천 생성에 성공했습니다."),
    TRAINING_PROGRESS_UPDATED(HttpStatus.OK, "트레이닝 진행 상황 업데이트에 성공했습니다."),
    VOCAL_ANALYSIS_REPORT_CREATED(HttpStatus.CREATED, "보컬 분석 리포트 생성에 성공했습니다."),

    SOLO_TRAINING_SESSION_FETCHED(HttpStatus.OK, "솔로 트레이닝 세션 정보 조회에 성공했습니다."),
    SOLO_TRAINING_SESSION_CREATED(HttpStatus.CREATED, "솔로 트레이닝 세션 생성에 성공했습니다."),
    SOLO_TRAINING_SESSION_DELETED(HttpStatus.OK, "솔로 트레이닝 종료 및 관련 데이터(사용자 보컬 오디오 파일 및 진행 중인 훈련 데이터)가 삭제되었습니다."),
    SOLO_MR_SONG_LIST_FETCHED(HttpStatus.OK, "솔로 트레이닝 MR 곡 목록 조회에 성공했습니다."),
    SOLO_ORIGINAL_SONG_LIST_FETCHED(HttpStatus.OK, "솔로 트레이닝 원곡 목록 조회에 성공했습니다."),
    SOLO_MR_SONG_FETCHED(HttpStatus.OK, "솔로 트레이닝 MR 곡 조회에 성공했습니다."),
    SOLO_ORIGINAL_SONG_FETCHED(HttpStatus.OK, "솔로 트레이닝 원곡 조회에 성공했습니다."),
    
    DUET_AUDIO_MERGED(HttpStatus.CREATED, "듀엣 음원 병합에 성공했습니다."),
    PARTNER_APPLICATION_CREATED(HttpStatus.CREATED, "파트너 신청이 완료되었습니다."),
    RECEIVED_PARTNER_APPLICATIONS_FETCHED(HttpStatus.OK, "받은 파트너 신청 목록 조회에 성공했습니다."),
    SENT_PARTNER_APPLICATIONS_FETCHED(HttpStatus.OK, "보낸 파트너 신청 목록 조회에 성공했습니다."),
    PARTNER_APPLICATION_ACCEPTED(HttpStatus.CREATED, "파트너 신청 수락 및 세션 생성에 성공했습니다."),
    PARTNER_APPLICATION_REJECTED(HttpStatus.OK, "파트너 신청 거절이 완료되었습니다."),
    DUET_TRAINING_ROOMS_FETCHED(HttpStatus.OK, "듀엣 트레이닝 방 목록 조회에 성공했습니다."),
    DUET_TRAINING_ROOM_CREATED(HttpStatus.CREATED, "듀엣 트레이닝 방 생성에 성공했습니다."),
    DUET_TRAINING_SESSION_FETCHED(HttpStatus.OK, "듀엣 트레이닝 세션 정보 조회에 성공했습니다."),
    DUET_TRAINING_SESSION_ENDED(HttpStatus.OK, "듀엣 트레이닝 세션 종료에 성공했습니다."),
    DUET_MR_SONG_LIST_FETCHED(HttpStatus.OK, "듀엣 트레이닝 MR 곡 목록 조회에 성공했습니다."),
    DUET_ORIGINAL_SONG_LIST_FETCHED(HttpStatus.OK, "듀엣 트레이닝 원곡 목록 조회에 성공했습니다."),
    DUET_MR_SONG_FETCHED(HttpStatus.OK, "듀엣 트레이닝 MR 곡 조회에 성공했습니다."),
    DUET_ORIGINAL_SONG_FETCHED(HttpStatus.OK, "듀엣 트레이닝 원곡 조회에 성공했습니다."),
    DUET_CURRENT_TRAINING_FETCHED(HttpStatus.OK, "현재 진행 중인 듀엣 트레이닝 훈련 목록 조회에 성공했습니다."),
    DUET_CURRENT_TRAINING_EMPTY(HttpStatus.NO_CONTENT, "진행 중인 트레이닝이 없습니다."),
    DUET_TRAINING_PROGRESS_UPDATED(HttpStatus.OK, "듀엣 트레이닝 진행 상황 업데이트에 성공했습니다."),

    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입에 성공했습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    SIGNUP_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "이메일, 비밀번호, 닉네임은 필수 항목입니다."),

    ;

    private final HttpStatus status;
    private final String message;
}