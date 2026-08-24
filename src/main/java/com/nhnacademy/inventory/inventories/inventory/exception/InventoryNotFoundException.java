package com.nhnacademy.inventory.inventories.inventory.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class InventoryNotFoundException extends BaseException {
    public InventoryNotFoundException() {
        super(InventoryErrorCode.INVENTORY_NOT_FOUND);
    }
}
