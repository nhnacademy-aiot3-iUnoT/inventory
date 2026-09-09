package com.nhnacademy.inventory.inventories.inventory.operation.inbound.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class ExpirationdateMismatchException extends BaseException {
    public ExpirationdateMismatchException() {
        super(InventoryErrorCode.INVENTORY_EXPIRATION_DATE_MISMATCH);
    }
}
