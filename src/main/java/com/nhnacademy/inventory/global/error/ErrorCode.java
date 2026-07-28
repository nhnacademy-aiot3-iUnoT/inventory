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

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 회원입니다."),

    //저장소 에러코드
    STORAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 저장소입니다."),
    STORAGE_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "S002", "조직내의 이미 존재하는 저장소 이름입니다."),

    //구역 에러코드
    ZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "Z001", "존재하지 않는 구역입니다."),
    ZONE_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Z002", "저장소내의 이미 존재하는 구역 이름입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}