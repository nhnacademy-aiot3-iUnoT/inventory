package com.nhnacademy.inventory.medicines.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MedicineErrorCode implements ErrorCode {


    //의약품 필수

    COMPANY_NAME_REQUIRED(HttpStatus.BAD_REQUEST,"M002","업체명은 필수입니다."),
    PRODUCT_NAME_REQUIRED(HttpStatus.BAD_REQUEST,"M005","제품명은 필수입니다."),

    // 품목기준 코드
    ITEM_CODE_REQUIRED(HttpStatus.BAD_REQUEST,"I001","품목기준코드는 필수입니다."),
    ITEM_CODE_INVALID(HttpStatus.BAD_REQUEST,"I002","품목기준코드는 숫자 9자리로 입력해야 합니다."),


    // 의약품
    MEDICINE_REQUIRED(HttpStatus.BAD_REQUEST,"M001","의약품은 필수입니다."),
    MEDICINE_NOT_FOUND(HttpStatus.NOT_FOUND,"M002","해당하는 의약품을 찾을 수 없습니다."),


    // 포장단위
    PACK_UNIT_NOT_FOUND(HttpStatus.NOT_FOUND,"P001","존재하지 않는 의약품 포장단위입니다."),
    PACK_UNIT_INVALID(HttpStatus.BAD_REQUEST,"P002","잘못된 입력값입니다."),
    PACK_UNIT_REQUIRED(HttpStatus.BAD_REQUEST,"P003","포장단위는 필수입니다."),


    // 의약품 검색
    MEDICINE_SEARCH_REQUEST_REQUIRED(HttpStatus.BAD_REQUEST,"S001","의약품 검색 요청 정보는 필수입니다."),
    MEDICINE_SEARCH_TYPE_REQUIRED(HttpStatus.BAD_REQUEST,"S002","조건 선택은 필수입니다."),


    // 환경
    ENVIRONMENT_RANGE_INVALID(HttpStatus.BAD_REQUEST,"E001","환경 기준의 최소값은 최대값보다 클 수 없습니다.");



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
