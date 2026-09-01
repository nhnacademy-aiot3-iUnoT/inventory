package com.nhnacademy.inventory.dashboards.exception;

import com.nhnacademy.inventory.dashboards.error.DashboardErrorCode;
import com.nhnacademy.inventory.global.error.BaseException;

public class DepartmentAccessDeniedException extends BaseException {
    public DepartmentAccessDeniedException() {
        super(DashboardErrorCode.DEPARTMENT_ACCESS_DENIED);
    }
}
