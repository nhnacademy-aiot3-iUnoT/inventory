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
    ZONE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Z002", "저장소내의 이미 존재하는 구역 이름입니다.");


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
