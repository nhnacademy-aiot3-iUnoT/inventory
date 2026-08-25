package com.nhnacademy.inventory.inventories.inventory.dto;


import com.nhnacademy.inventory.global.dto.PageResponse;


public record InventoryInfoResponse(

        Long medicinePackUnitId,
        Long storageId,
        String storageName,
        String itemCode,
        String productName,
        PageResponse<InventoryDetailResponse> inventories


) {

    public static InventoryInfoResponse from(InventoryResponse response,PageResponse<InventoryDetailResponse> details){

        return new InventoryInfoResponse(
                response.medicinePackUnitId(),
                response.storageId(),
                response.storageName(),
                response.itemCode(),
                response.productName(),
                details
                );

    }



}
