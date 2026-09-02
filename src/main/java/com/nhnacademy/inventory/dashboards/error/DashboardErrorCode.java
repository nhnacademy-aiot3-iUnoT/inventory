package com.nhnacademy.inventory.dashboards.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum DashboardErrorCode implements ErrorCode {

    DEPARTMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DB001", "해당 부서의 현황을 조회할 권한이 없습니다."),
    ORG_WIDE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DB002", "조직 전체 현황은 관리자만 조회할 수 있습니다."),
    STORAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DB003", "해당 저장소의 환경 현황을 조회할 권한이 없습니다.");

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
