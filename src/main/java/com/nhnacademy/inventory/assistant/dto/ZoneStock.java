package com.nhnacademy.inventory.assistant.dto;

import java.time.LocalDate;

public record ZoneStock(
        Long zoneId,
        String zoneName,
        Long quantity,
        LocalDate earliestExpirationDate
) {
}
