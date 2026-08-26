package com.nhnacademy.inventory.inventories.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    //최소재고 임계값 에러코드
    STOCK_THRESHOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "ST003", "존재하지 않는 최소재고 임계값 입니다."),
    STOCK_THRESHOLD_ALREADY_EXISTS(HttpStatus.CONFLICT, "ST004", "이미 존재하는 최소재고 임계값입니다."),

    //환경
    ENVIRONMENT_STANDARD_NOT_FOUND(HttpStatus.NOT_FOUND,"E001","해당하는 환경 기준이 존재하지 않습니다."),
    ENVIRONMENT_STANDARD_CONFLICT(HttpStatus.CONFLICT,"E002","조회 이후 환경기준이 변경되었습니다. 최신 정보를 확인 후 다시 시도해주세요."),


    //알림 에러코드
    ALERT_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "존재하지 않거나 이미 삭제된 알림입니다."),

    //검토 내역 에러코드
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "존재하지 않는 검토내역 입니다."),


    // zone - packUnitId
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND,"ZP001","해당하는 의약품이 존재하지 않습니다.");



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
