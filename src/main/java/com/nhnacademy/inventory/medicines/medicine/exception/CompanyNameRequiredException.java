package com.nhnacademy.inventory.medicines.medicine.exception;


import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class CompanyNameRequiredException extends BaseException {
    public CompanyNameRequiredException(ErrorCode errorCode) {
        super(errorCode);
    }
}
