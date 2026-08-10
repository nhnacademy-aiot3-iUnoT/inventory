package com.nhnacademy.inventory.medicines.enviroment.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class EnvironmentRangeInvalidException extends BaseException {
    public EnvironmentRangeInvalidException() {
        super(MedicineErrorCode.ENVIRONMENT_RANGE_INVALID);
    }
}
