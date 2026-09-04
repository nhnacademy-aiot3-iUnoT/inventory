package com.nhnacademy.inventory.enviroments.review.dto;

import com.nhnacademy.inventory.enviroments.review.domain.EnvironmentReview;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewHistorySummaryResponse(
        Long environmentReviewId,
        Long inventoryId,
        Long medicineId,
        Long medicinePackageUnitId,
        String productName,
        String packUnit,
        LocalDateTime createdAt,
        UUID reviewerId,
        String reviewerName,
        Boolean isOut


) {
    @QueryProjection
    public ReviewHistorySummaryResponse{

    }

    public static ReviewHistorySummaryResponse from(EnvironmentReview review){
        return new ReviewHistorySummaryResponse(
                review.getId(),
                review.getMedicineInventory().getId(),
                review.getMedicineInventory().getMedicinePackageUnit().getMedicine().getId(),
                review.getMedicineInventory().getMedicinePackageUnit().getId(),
                review.getMedicineInventory().getMedicinePackageUnit().getMedicine().getProductName(),
                review.getMedicineInventory().getMedicinePackageUnit().getPackUnit(),
                review.getCreatedAt(),
                review.getReviewerId(),
                null,
                review.getIsOut()
        );
    }

    public static ReviewHistorySummaryResponse of(ReviewHistorySummaryResponse response, String reviewerName){
        return new ReviewHistorySummaryResponse(
                response.environmentReviewId,
                response.inventoryId,
                response.medicineId,
                response.medicinePackageUnitId,
                response.productName,
                response.packUnit,
                response.createdAt,
                response.reviewerId,
                reviewerName,
                response.isOut
        );
    }
}
