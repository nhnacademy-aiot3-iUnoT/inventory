package com.nhnacademy.inventory.inventories.inventory.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class InsufficientStockException extends BaseException {

    public InsufficientStockException() {
        super(InventoryErrorCode.INSUFFICIENT_STOCK);
    }
}
