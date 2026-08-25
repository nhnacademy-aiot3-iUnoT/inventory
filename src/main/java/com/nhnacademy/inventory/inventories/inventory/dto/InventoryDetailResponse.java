package com.nhnacademy.inventory.inventories.inventory.dto;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;

import java.time.LocalDate;

public record InventoryDetailResponse(

        Long inventoryId,
        Long zoneId,
        String zoneName,
        String lotNumber,
        LocalDate expirationDate,
        Integer currentQuantity,
        ManagementStatus managementStatus


) {

    public static InventoryDetailResponse from(InventoryResponse response){

        return new InventoryDetailResponse(
                response.inventoryId(),
                response.zoneId(),
                response.zoneName(),
                response.lotNumber(),
                response.expirationDate(),
                response.currentQuantity(),
                response.managementStatus()
                );
    }



}
