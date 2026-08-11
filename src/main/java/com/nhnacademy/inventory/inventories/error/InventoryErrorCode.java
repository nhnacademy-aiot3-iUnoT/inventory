package com.nhnacademy.inventory.inventories.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {


    //환경
    ENVIRONMENT_STANDARD_NOT_FOUND(HttpStatus.NOT_FOUND,"E001","해당하는 환경 기준이 존재하지 않습니다.");





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
