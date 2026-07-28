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
    PRODUCT_NAME_REQUIRED(HttpStatus.BAD_REQUEST,"M005","제품명은 필수입니다.");


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
