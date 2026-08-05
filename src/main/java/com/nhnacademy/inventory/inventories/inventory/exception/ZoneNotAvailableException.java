package com.nhnacademy.inventory.inventories.inventory.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class ZoneNotAvailableException extends BaseException {
    public ZoneNotAvailableException() {
        super(OrganizationErrorCode.ZONE_NOT_AVAILABLE);
    }
}
