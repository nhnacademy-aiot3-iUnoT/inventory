package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class ZoneInactiveException extends BaseException {
    public ZoneInactiveException() {
        super(OrganizationErrorCode.ZONE_INACTIVE);
    }
}
