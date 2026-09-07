package com.nhnacademy.inventory.assistant.event;

import java.util.UUID;

public record StockOutboundInspectionEvent(
        UUID actorUuid,
        Long zoneId,
        Long medicinePackageUnitId,
        int quantity
) {
}
