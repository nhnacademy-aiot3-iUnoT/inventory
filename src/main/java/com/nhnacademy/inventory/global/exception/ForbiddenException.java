package com.nhnacademy.inventory.global.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class ForbiddenException extends BaseException {
    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }
}
