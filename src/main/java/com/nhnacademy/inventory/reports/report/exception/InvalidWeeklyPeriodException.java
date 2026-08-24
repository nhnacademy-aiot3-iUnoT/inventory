package com.nhnacademy.inventory.reports.report.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.reports.error.ReportErrorCode;

public class InvalidWeeklyPeriodException extends BaseException {
    public InvalidWeeklyPeriodException() {
        super(ReportErrorCode.INVALID_WEEKLY_PERIOD);
    }
}
