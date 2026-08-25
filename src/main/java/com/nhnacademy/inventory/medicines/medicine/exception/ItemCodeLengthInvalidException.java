package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class ItemCodeLengthInvalidException extends BaseException {
    public ItemCodeLengthInvalidException() {
        super(MedicineErrorCode.ITEM_CODE_LENGTH_INVALID);
    }
}
