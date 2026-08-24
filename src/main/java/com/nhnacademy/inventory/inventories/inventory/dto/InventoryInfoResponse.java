package com.nhnacademy.inventory.inventories.inventory.dto;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record InventoryInfoResponse(


        Long medicinePackUnitId,
        Long storageId,
        Long zoneId,
        String productName,
        String lotNumber,
        LocalDate expirationDate,
        Integer currentQuantity,
        String storageName,
        String zoneName,
        ManagementStatus managementStatus


) {


    @QueryProjection
    public InventoryInfoResponse{

    }


}
