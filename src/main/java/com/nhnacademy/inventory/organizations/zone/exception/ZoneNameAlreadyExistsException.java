package com.nhnacademy.inventory.organizations.zone.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class ZoneNameAlreadyExistsException extends BaseException {
    public ZoneNameAlreadyExistsException() {
        super(ErrorCode.ZONE_NAME_ALREADY_EXISTS);
    }
}
