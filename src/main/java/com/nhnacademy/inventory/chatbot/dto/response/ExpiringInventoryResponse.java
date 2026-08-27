package com.nhnacademy.inventory.chatbot.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ExpiringInventoryResponse(
        int searchDays,
        List<Item> items
) {
    public record Item(
            String productName,
            String packUnit,
            String lotNumber,
            LocalDate expirationDate,
            int daysUntilExpiration,
            int quantity,
            String storageName,
            String zoneName
    ) {}
}
