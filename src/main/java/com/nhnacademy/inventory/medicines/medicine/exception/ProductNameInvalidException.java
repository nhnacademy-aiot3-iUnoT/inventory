package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class ProductNameInvalidException extends BaseException {
    public ProductNameInvalidException() {
        super(MedicineErrorCode.PRODUCT_NAME_LENGTH_INVALID);
    }
}
