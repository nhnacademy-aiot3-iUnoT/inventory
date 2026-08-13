package com.nhnacademy.inventory.inventories.threshold.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class StockThresholdAlreadyExistsException extends BaseException {
    public StockThresholdAlreadyExistsException() {
        super(InventoryErrorCode.STOCK_THRESHOLD_ALREADY_EXISTS);
    }
}
