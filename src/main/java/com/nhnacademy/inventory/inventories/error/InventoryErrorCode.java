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
    ENVIRONMENT_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "ER001", "존재하지 않는 검토내역 입니다."),


    // zone - packUnitId
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND,"ZP001","해당하는 의약품이 존재하지 않습니다."),


    // 유통기한
    INVENTORY_EXPIRATION_DATE_MISMATCH(HttpStatus.BAD_REQUEST,"EX001","동일 제조번호의 유통기한이 기존 재고와 다릅니다."),

    // 재고변동내역
    TRANSACTION_TYPE_INVALID(HttpStatus.BAD_REQUEST,"T001","입고 처리에 사용할 수 없는 유형입니다."),



    // 출고 에러코드
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "OD001", "출고 가능 재고가 부족합니다."),
    INVALID_OUTBOUND_TYPE(HttpStatus.BAD_REQUEST, "OD002", "출고 처리에 사용할 수 없는 유형입니다."),
    DISPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "OD003", "폐기할 재고를 찾을 수 없습니다."),
    INVALID_EXPIRED_DISPOSAL_TARGET(HttpStatus.CONFLICT, "OD004", "유통기한 경과 상태이며 수량이 남아 있는 재고만 선택 폐기할 수 있습니다.");

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
