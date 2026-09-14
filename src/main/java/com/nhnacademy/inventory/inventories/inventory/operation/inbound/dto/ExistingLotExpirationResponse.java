package com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto;

import java.time.LocalDate;

public record ExistingLotExpirationResponse(
        boolean exists,
        LocalDate expirationDate
) {
    public static ExistingLotExpirationResponse empty() {
        return new ExistingLotExpirationResponse(false, null);
    }

    public static ExistingLotExpirationResponse from(
            LocalDate expirationDate
    ) {
        return new ExistingLotExpirationResponse(true, expirationDate);
    }
}