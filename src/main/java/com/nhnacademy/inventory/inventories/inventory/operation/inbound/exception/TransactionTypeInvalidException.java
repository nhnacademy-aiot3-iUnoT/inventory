package com.nhnacademy.inventory.inventories.inventory.operation.inbound.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class TransactionTypeInvalidException extends BaseException {
    public TransactionTypeInvalidException() {
        super(InventoryErrorCode.TRANSACTION_TYPE_INVALID);
    }
}
