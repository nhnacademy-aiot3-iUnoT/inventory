package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class ZoneNameAlreadyExistsException extends BaseException {
    public ZoneNameAlreadyExistsException() {
        super(OrganizationErrorCode.ZONE_NAME_ALREADY_EXISTS);
    }
}
