package com.nhnacademy.inventory.enviroments.review.dto;

import com.nhnacademy.inventory.enviroments.event.dto.EnvironmentEventItemResponse;
import com.nhnacademy.inventory.enviroments.review.domain.EnvironmentReview;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReviewHistoryDetailResponse(
        Long environmentReviewId,
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
        LocalDateTime createdAt,
        UUID reviewerId,
        String reviewerName,
        Boolean isOut,
        Integer quantityAtReview,
        String memo,

        List<ReviewHistorySummaryResponse> inventoryReviewHistories,
        List<EnvironmentEventItemResponse> environmentEvents
) {
    public static ReviewHistoryDetailResponse from(
            EnvironmentReview review,
            List<ReviewHistorySummaryResponse> inventoryReviewHistories,
            List<EnvironmentEventItemResponse> environmentEvents,
            String reviewerName
    ){
        MedicineInventory medicineInventory = review.getMedicineInventory();

        return new ReviewHistoryDetailResponse(
                review.getId(),
                medicineInventory.getId(),
                medicineInventory.getZone().getStorage().getOrganization().getId(),
                medicineInventory.getZone().getStorage().getId(),
                medicineInventory.getZone().getId(),
                medicineInventory.getMedicinePackageUnit().getMedicine().getId(),
                medicineInventory.getMedicinePackageUnit().getId(),
                medicineInventory.getZone().getStorage().getOrganization().getName(),
                medicineInventory.getZone().getStorage().getName(),
                medicineInventory.getZone().getName(),
                medicineInventory.getMedicinePackageUnit().getMedicine().getProductName(),
                medicineInventory.getMedicinePackageUnit().getPackUnit(),
                medicineInventory.getLotNumber(),
                medicineInventory.getCreatedAt(),
                review.getReviewerId(),
                reviewerName,
                review.getIsOut(),
                review.getQuantityAtReview(),
                review.getMemo(),

                inventoryReviewHistories,
                environmentEvents
        );
    }
}
