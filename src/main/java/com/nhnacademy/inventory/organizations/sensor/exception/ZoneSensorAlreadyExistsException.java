package com.nhnacademy.inventory.organizations.sensor.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class ZoneSensorAlreadyExistsException extends BaseException {
    public ZoneSensorAlreadyExistsException() {
        super(OrganizationErrorCode.ZONE_SENSOR_ALREADY_EXISTS);
    }
}
