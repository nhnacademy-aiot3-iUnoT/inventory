package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class ZoneNotFoundException extends BaseException {
    public ZoneNotFoundException() {
        super(ErrorCode.ZONE_NOT_FOUND);
    }
}
