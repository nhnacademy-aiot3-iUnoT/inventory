package com.nhnacademy.inventory.dashboards.exception;

import com.nhnacademy.inventory.dashboards.error.DashboardErrorCode;
import com.nhnacademy.inventory.global.error.BaseException;

public class StorageAccessDeniedException extends BaseException {
    public StorageAccessDeniedException() {
        super(DashboardErrorCode.STORAGE_ACCESS_DENIED);
    }
}
