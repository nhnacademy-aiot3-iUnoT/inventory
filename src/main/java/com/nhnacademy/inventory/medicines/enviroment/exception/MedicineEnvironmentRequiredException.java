package com.nhnacademy.inventory.medicines.enviroment.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class MedicineEnvironmentRequiredException extends BaseException {
    public MedicineEnvironmentRequiredException() {
        super(MedicineErrorCode.MEDICINE_ENVIRONMENT_REQUIRED);
    }
}
