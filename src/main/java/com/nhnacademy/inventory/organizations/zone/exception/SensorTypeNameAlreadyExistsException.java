package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class SensorTypeNameAlreadyExistsException extends BaseException {
    public SensorTypeNameAlreadyExistsException() {
        super(OrganizationErrorCode.SENSOR_TYPE_NAME_ALREADY_EXISTS);
    }
}
