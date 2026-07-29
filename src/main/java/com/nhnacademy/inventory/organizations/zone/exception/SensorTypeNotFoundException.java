package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class SensorTypeNotFoundException extends BaseException {
    public SensorTypeNotFoundException() {
        super(OrganizationErrorCode.SENSOR_TYPE_NOT_FOUND);
    }
}
