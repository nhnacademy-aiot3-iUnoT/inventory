package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class ItemCodeInvalidException extends BaseException {
    public ItemCodeInvalidException() {
        super(MedicineErrorCode.ITEM_CODE_INVALID);
    }
}
