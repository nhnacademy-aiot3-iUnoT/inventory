package com.nhnacademy.inventory.organizations.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OrganizationErrorCode implements ErrorCode {

    //저장소 에러코드
    STORAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 저장소입니다."),
    STORAGE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "S002", "조직내의 이미 존재하는 저장소 이름입니다."),
    STORAGE_INACTIVE(HttpStatus.CONFLICT, "S004","비활성화된 저장소 입니다."),

    //구역 에러코드
    ZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "Z001", "존재하지 않는 구역입니다."),
    ZONE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Z002", "저장소내의 이미 존재하는 구역 이름입니다."),
    ZONE_NOT_AVAILABLE(HttpStatus.CONFLICT,"Z003","이용할 수 없는 구역입니다."),
    ZONE_INACTIVE(HttpStatus.CONFLICT, "Z004", "비활성화된 저장소 입니다."),


    //센서타입 에러코드
    SENSOR_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "ST001", "존재하지 않는 센서타입 입니다."),
    SENSOR_TYPE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "ST002", "이미 존재하는 센서타입 이름입니다."),

    //구역센서 에러코드
    ZONE_SENSOR_NOT_FOUND(HttpStatus.NOT_FOUND, "ZS001", "존재하지 않는 센서 입니다."),
    ZONE_SENSOR_ALREADY_EXISTS(HttpStatus.CONFLICT, "ZS002", "이미 등록된 센서입니다."),

    //임계값 에러코드
    THRESHOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "존재하지 않는 임계설정 입니다."),
    THRESHOLD_INVALID_RANGE(HttpStatus.BAD_REQUEST, "T003", "잘못된 범위의 임계값 입니다."),

    // 조직/조직원 에러코드
    ORG_ALREADY_EXISTS(HttpStatus.CONFLICT, "O001", "이미 존재하는 조직입니다."),
    ORG_NOT_FOUND(HttpStatus.NOT_FOUND, "O002", "존재하지 않는 조직입니다."),
    USER_ORG_NOT_FOUND(HttpStatus.NOT_FOUND, "O003", "소속된 조직이 없습니다."),
    ORG_STATUS_INVALID(HttpStatus.BAD_REQUEST, "O004", "허용되지 않는 조직 상태 변경 요청입니다."),
    ORG_ALREADY_SETUP(HttpStatus.CONFLICT, "O005", "이미 활성화된 조직입니다."),
    ORG_ALREADY_SUSPENDED(HttpStatus.CONFLICT, "O006", "이미 종료된 조직입니다."),
    ORG_NOT_ACTIVE(HttpStatus.CONFLICT, "O007", "현재 조직 상태에서는 해당 작업을 수행할 수 없습니다."),

    ORG_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "OM001", "존재하지 않는 조직원입니다."),
    ORG_MEMBER_ROLE_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "OM002", "해당 Role로 변경할 수 없습니다."),
    ORG_BOSS_LEAVE_NOT_ALLOWED(HttpStatus.CONFLICT, "OM003", "조직장은 조직을 탈퇴할 수 없습니다."),

    // 초대 에러코드
    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "J001", "초대 내역을 찾을 수 없습니다."),
    INVITATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "J002", "초대 내역이 이미 존재합니다."),
    INVITATION_EMAIL_MIS_MISMATCH(HttpStatus.BAD_REQUEST, "J003", "입력하신 이메일과 초대 받은 이메일이 일치하지 않습니다."),
    INVITATION_ALREADY_EXPIRED(HttpStatus.BAD_REQUEST, "J004", "이미 만료된 초대 토큰입니다."),
    INVITATION_INVALID(HttpStatus.BAD_REQUEST, "J005", "유효하지 않거나 이미 처리된 초대입니다."),

    // 부서 에러코드
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "부서를 찾을 수 없습니다."),
    DEPARTMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "D002", "이미 존재하는 부서 입니다."),

    // 알림설정 에러코드
    NOTIFICATION_PREFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "NP001", "알림 설정을 찾을 수 없습니다."),
    NOTIFICATION_PREFERENCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "NP002", "이미 존재하는 알림 설정입니다."),
    NOTIFICATION_PREFERENCE_INVALID_SCOPE(HttpStatus.BAD_REQUEST, "NP003", "알림 설정 범위가 올바르지 않습니다."),
    NOTIFICATION_RECIPIENT_REQUIRED(HttpStatus.BAD_REQUEST, "NP004", "알림 수신자 정보가 필요합니다"),

    // 부서 단톡방 에러코드
    DEPARTMENT_TELEGRAM_CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "DT001", "부서에 연결된 단톡방이 없습니다."),
    DEPARTMENT_TELEGRAM_CHAT_ALREADY_LINKED(HttpStatus.CONFLICT, "DT002", "이미 다른 부서에 연결된 단톡방입니다.");








    private final HttpStatus status;
    private final String code;
    private final String message;


    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getName() {
        return this.name();
    }


}
