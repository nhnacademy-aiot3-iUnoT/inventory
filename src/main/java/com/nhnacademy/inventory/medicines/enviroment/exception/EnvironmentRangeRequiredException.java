package com.nhnacademy.inventory.medicines.enviroment.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class EnvironmentRangeRequiredException extends BaseException {
    public EnvironmentRangeRequiredException() {
        super(MedicineErrorCode.ENVIRONMENT_RANGE_REQUIRED);
    }
}
