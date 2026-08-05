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
    ZONE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST,"Z003","현재 사용할 수 없는 보관 구역입니다."),

    //센서타입 에러코드
    SENSOR_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "ST001", "존재하지 않는 센서타입 입니다."),
    SENSOR_TYPE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "ST002", "이미 존재하는 센서타입 이름입니다."),

    //임계값 에러코드
    THRESHOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "존재하지 않는 임계설정 입니다."),
    THRESHOLD_INVALID_RANGE(HttpStatus.BAD_REQUEST, "T003", "잘못된 범위의 임계값 입니다."),


    // 조직원
    ORGANIZATION_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"O001","조직 소속 정보를 찾을 수 없습니다.");



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
