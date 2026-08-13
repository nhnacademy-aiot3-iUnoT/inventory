package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class ZoneNotFoundException extends BaseException {
    public ZoneNotFoundException() {
        super(OrganizationErrorCode.ZONE_NOT_FOUND);
    }
}
