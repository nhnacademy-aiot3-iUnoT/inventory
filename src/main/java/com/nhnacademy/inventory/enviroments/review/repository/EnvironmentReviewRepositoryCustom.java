package com.nhnacademy.inventory.enviroments.review.repository;

import com.nhnacademy.inventory.enviroments.review.domain.EnvironmentReview;
import com.nhnacademy.inventory.enviroments.review.dto.ReviewHistorySummaryResponse;
import com.nhnacademy.inventory.enviroments.review.dto.UnderReviewInventoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EnvironmentReviewRepositoryCustom {
    Page<UnderReviewInventoryResponse> getUnderReviewInventories(
            List<Long> storageIds,
            Pageable pageable
    );

    Page<ReviewHistorySummaryResponse> getReviewHistories(
            List<Long> storageIds,
            Pageable pageable
    );

    Optional<EnvironmentReview> findByIdWithFetch(Long id);
}
