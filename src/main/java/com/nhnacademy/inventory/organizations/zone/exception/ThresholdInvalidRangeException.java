package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class ThresholdInvalidRangeException extends BaseException {
    public ThresholdInvalidRangeException() {
        super(OrganizationErrorCode.THRESHOLD_INVALID_RANGE);
    }
}
