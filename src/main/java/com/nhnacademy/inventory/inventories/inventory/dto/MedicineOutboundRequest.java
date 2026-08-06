package com.nhnacademy.inventory.inventories.inventory.dto;

public record MedicineOutboundRequest(

        Long inventoryId,
        String reason,
        String memo


) {
}
