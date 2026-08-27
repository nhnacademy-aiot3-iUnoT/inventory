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
        Boolean isOut


) {
    @QueryProjection
    public ReviewHistorySummaryResponse{

    }
}
