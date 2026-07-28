package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class PackUnitRequiredException extends BaseException {
    public PackUnitRequiredException(ErrorCode errorCode) {
        super(errorCode);
    }
}
