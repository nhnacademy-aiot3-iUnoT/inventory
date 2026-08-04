package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class PackUnitIdInvalidException extends BaseException {
    public PackUnitIdInvalidException() {
        super(MedicineErrorCode.PACK_UNIT_INVALID);
    }
}
