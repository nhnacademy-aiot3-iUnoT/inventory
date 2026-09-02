package com.nhnacademy.inventory.dashboards.exception;

import com.nhnacademy.inventory.dashboards.error.DashboardErrorCode;
import com.nhnacademy.inventory.global.error.BaseException;

public class OrgWideAccessDeniedException extends BaseException {
    public OrgWideAccessDeniedException() {
        super(DashboardErrorCode.ORG_WIDE_ACCESS_DENIED);
    }
}
