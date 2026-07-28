package com.nhnacademy.inventory.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "G001", "잘못된 입력값입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "G002", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G003", "서버 오류가 발생했습니다."),

    // 회원 관련 에러 예시
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 회원입니다."),


    // 의약품
    MEDICINE_REQUIRED(HttpStatus.BAD_REQUEST,"M001","의약품은 필수입니다."),
    COMPANYNAME_REQUIRED(HttpStatus.BAD_REQUEST,"M002","업체명은 필수입니다."),
    ITEMCODE_REQUIRED(HttpStatus.BAD_REQUEST,"M003","품목기준코드는 필수입니다."),
    PACKUNIT_REQUIRED(HttpStatus.BAD_REQUEST,"M004","포장단위는 필수입니다."),
    PRODUCTNAME_REQUIRED(HttpStatus.BAD_REQUEST,"M005","제품명은 필수입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}