package com.nhnacademy.inventory.organizations.sensor.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class ZoneSensorNotFoundException extends BaseException {
    public ZoneSensorNotFoundException() {
        super(OrganizationErrorCode.ZONE_SENSOR_NOT_FOUND);
    }
}
