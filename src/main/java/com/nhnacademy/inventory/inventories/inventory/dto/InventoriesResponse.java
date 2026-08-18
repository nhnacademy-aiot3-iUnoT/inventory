package com.nhnacademy.inventory.inventories.inventory.dto;

import com.querydsl.core.annotations.QueryProjection;


import java.time.LocalDate;

public record InventoriesResponse(

        Long storageId,
        Long packUnitId,
        String productName,
        String itemCode,
        String packUnit,
        LocalDate expirationDate,
        String storageName,
        Integer totalQuantity


) {

    public static InventoriesResponse from(
            Long storageId, Long packUnitId,String productName, String itemCode,String packUnit,LocalDate expirationDate,String storageName,Integer totalQuantity){
        return new InventoriesResponse(
                storageId,
                packUnitId,
                productName,
                itemCode,
                packUnit,
                expirationDate,
                storageName,
                totalQuantity
        );
    }

    @QueryProjection
    public InventoriesResponse{

    }

}
