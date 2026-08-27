package com.nhnacademy.inventory.chatbot.dto.query;

import java.time.LocalDate;
import java.util.List;

public record FindExpiringInventoryQuery(
        List<Long> storageIds,
        LocalDate today,
        LocalDate limitDate,
        String medicineName,
        String storageName,
        String zoneName,
        int limit
) {
}
