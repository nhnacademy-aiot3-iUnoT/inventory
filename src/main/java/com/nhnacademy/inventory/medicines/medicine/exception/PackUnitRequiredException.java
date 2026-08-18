package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class PackUnitRequiredException extends BaseException {
    public PackUnitRequiredException() {
        super(MedicineErrorCode.PACK_UNIT_REQUIRED);
    }
}
