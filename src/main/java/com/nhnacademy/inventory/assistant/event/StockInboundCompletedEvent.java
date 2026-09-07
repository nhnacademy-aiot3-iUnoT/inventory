package com.nhnacademy.inventory.assistant.event;

import java.time.LocalDate;
import java.util.UUID;

public record StockInboundCompletedEvent(
        UUID actorUuid,
        Long zoneId,
        Long medicinePackageUnitId,
        LocalDate expirationDate,
        int quantity
) {}
