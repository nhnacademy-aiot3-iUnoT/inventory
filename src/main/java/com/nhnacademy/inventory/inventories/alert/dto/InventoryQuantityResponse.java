package com.nhnacademy.inventory.inventories.alert.dto;

import com.querydsl.core.annotations.QueryProjection;

public record InventoryQuantityResponse(

        Long storageId,
        Long medicinePackUnitId,
        Integer totalQuantity


) {

    @QueryProjection
    public InventoryQuantityResponse{

    }



}
