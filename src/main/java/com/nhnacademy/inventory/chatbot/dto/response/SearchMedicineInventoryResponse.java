package com.nhnacademy.inventory.chatbot.dto.response;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;

import java.time.LocalDate;
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
            int quantity,
            Long inventoryId,
            String lotNumber,
            LocalDate expirationDate,
            ManagementStatus managementStatus
    ) {}
}