package com.nhnacademy.inventory.medicines.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MedicineErrorCode implements ErrorCode {


    // 의약품
    MEDICINE_REQUIRED(HttpStatus.BAD_REQUEST,"M001","의약품은 필수입니다."),
    COMPANY_NAME_REQUIRED(HttpStatus.BAD_REQUEST,"M002","업체명은 필수입니다."),
    ITEM_CODE_REQUIRED(HttpStatus.BAD_REQUEST,"M003","품목기준코드는 필수입니다."),
    PACK_UNIT_REQUIRED(HttpStatus.BAD_REQUEST,"M004","포장단위는 필수입니다."),
    PRODUCT_NAME_REQUIRED(HttpStatus.BAD_REQUEST,"M005","제품명은 필수입니다."),

    MEDICINE_NOT_FOUND(HttpStatus.NOT_FOUND,"M006","해당하는 의약품을 찾을 수 없습니다."),
    PACK_UNIT_NOT_FOUND(HttpStatus.NOT_FOUND,"M007","존재하지 않는 의약품 포장단위입니다."),
    PACK_UNIT_INVALID(HttpStatus.BAD_REQUEST,"M008","잘못된 입력값입니다."),
    ITEM_CODE_INVALID(HttpStatus.BAD_REQUEST,"M009","품목기준코드는 숫자 9자리로 입력해야 합니다."),

    MEDICINE_SEARCH_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST,"M010","의약품 검색 요청 정보는 필수입니다."),
    MEDICINE_SEARCH_TYPE_REQUIRED(HttpStatus.BAD_REQUEST,"M11","조건 선택은 필수입니다.");


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
