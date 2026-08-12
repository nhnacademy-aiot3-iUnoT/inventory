package com.nhnacademy.inventory.inventories.threshold.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class StockThresholdNotFoundException extends BaseException {
    public StockThresholdNotFoundException() {
        super(InventoryErrorCode.STOCK_THRESHOLD_NOT_FOUND);
    }
}
