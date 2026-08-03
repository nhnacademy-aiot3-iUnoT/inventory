package com.nhnacademy.inventory.organizations.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OrganizationErrorCode implements ErrorCode {

    //저장소 에러코드
    STORAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 저장소입니다."),
    STORAGE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "S002", "조직내의 이미 존재하는 저장소 이름입니다."),

    //구역 에러코드
    ZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "Z001", "존재하지 않는 구역입니다."),
    ZONE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Z002", "저장소내의 이미 존재하는 구역 이름입니다."),

    //센서타입 에러코드
    SENSOR_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "ST001", "존재하지 않는 센서타입 입니다."),
    SENSOR_TYPE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "ST002", "이미 존재하는 센서타입 이름입니다."),

    //임계값 에러코드
    THRESHOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "존재하지 않는 임계설정 입니다."),
    THRESHOLD_INVALID_RANGE(HttpStatus.BAD_REQUEST, "T003", "잘못된 범위의 임계값 입니다."),
    // 조직 에러코드
    ORG_ALREADY_EXISTS(HttpStatus.CONFLICT, "O001", "이미 존재하는 조직입니다."),
    ORG_NOT_FOUND(HttpStatus.NOT_FOUND, "O002", "존재하지 않는 조직입니다."),
    USER_ORG_NOT_FOUND(HttpStatus.NOT_FOUND, "O003", "소속된 조직이 없습니다."),
    ORG_STATUS_INVALID(HttpStatus.BAD_REQUEST, "O004", "허용되지 않는 조직 상태 변경 요청입니다."),
    ORG_ALREADY_COMPLETE(HttpStatus.CONFLICT, "O005", "이미 활성화된 조직입니다."),

    // 초대 에러코드
    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "J001", "초대 내역을 찾을 수 없습니다."),
    INVITATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "J002", "초대 내역이 이미 존재합니다.");

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
