package com.nhnacademy.inventory.chatbot.dto.response;

import java.util.List;

public record LowStockInventoryResponse(
        List<Item> items
) {
    public record Item(
            String productName,
            String packUnit,
            int currentQuantity,
            int threshold,
            String storageName,
            String zoneName
    ) {}
}
