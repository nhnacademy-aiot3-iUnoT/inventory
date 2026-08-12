package com.nhnacademy.inventory.reports.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {
    INVALID_WEEKLY_PERIOD(HttpStatus.BAD_REQUEST, "R001", "주간 리포트는 월요일부터 시작해야 합니다."),
    INVALID_MONTHLY_PERIOD(HttpStatus.BAD_REQUEST, "R002", "월간 리포트는 매월 1일부터 시작해야 합니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "R003", "존재하지 않는 리포트입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
