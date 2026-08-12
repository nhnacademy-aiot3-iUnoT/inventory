package com.nhnacademy.inventory.inventories.alert.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class AlertNotFoundException extends BaseException {
    public AlertNotFoundException() {
        super(InventoryErrorCode.ALERT_NOT_FOUND);
    }
}
