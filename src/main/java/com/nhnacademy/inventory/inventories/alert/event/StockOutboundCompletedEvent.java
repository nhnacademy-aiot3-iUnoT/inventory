package com.nhnacademy.inventory.inventories.alert.event;

public record StockOutboundCompletedEvent(
        Long zoneId,
        Long medicinePackageUnitId
) {
}
