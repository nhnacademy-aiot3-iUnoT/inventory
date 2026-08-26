package com.nhnacademy.inventory.enviroments.review.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UnderReviewInventoryResponse(
        Long inventoryId,
        Long organizationId,
        Long storageId,
        Long zoneId,
        Long medicineId,
        Long medicinePackageUnitId,
        String organizationName,
        String storageName,
        String zoneName,
        String productName,
        String packUnit,
        String lotNumber,
        LocalDate expirationDate,
        Integer currentQuantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastReviewAt
) {
    @QueryProjection
    public UnderReviewInventoryResponse{

    }
}
