package com.nhnacademy.inventory.medicines.enviroment.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class EnvironmentStandardNotFoundException extends BaseException {
    public EnvironmentStandardNotFoundException() {
        super(InventoryErrorCode.ENVIRONMENT_STANDARD_NOT_FOUND);
    }
}
