package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class ThresholdNotFoundException extends BaseException {
    public ThresholdNotFoundException() {
        super(OrganizationErrorCode.THRESHOLD_NOT_FOUND);
    }
}
