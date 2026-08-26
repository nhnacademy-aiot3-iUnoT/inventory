package com.nhnacademy.inventory.chatbot.dto.response;

import java.util.List;

public record SearchMedicineInventoryResponse(
        List<MedicineInventoryItem> items
) {
    public record MedicineInventoryItem(
            String productName,
            String packUnit,
            int totalQuantity,
            List<Location> locations
    ) {}

    public record Location(
            String storageName,
            String zoneName,
            int quantity
    ) {}
}
