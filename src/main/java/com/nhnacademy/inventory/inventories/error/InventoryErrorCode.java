package com.nhnacademy.inventory.inventories.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    //최소재고 임계값 에러코드
    STOCK_THRESHOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "ST003", "존재하지 않는 최소재고 임계값 입니다."),
    STOCK_THRESHOLD_ALREADY_EXISTS(HttpStatus.CONFLICT, "ST004", "이미 존재하는 최소재고 임계값입니다.");


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
