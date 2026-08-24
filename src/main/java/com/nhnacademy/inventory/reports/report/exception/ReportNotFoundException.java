package com.nhnacademy.inventory.reports.report.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.reports.error.ReportErrorCode;

public class ReportNotFoundException extends BaseException {
    public ReportNotFoundException() {
        super(ReportErrorCode.REPORT_NOT_FOUND);
    }
}
