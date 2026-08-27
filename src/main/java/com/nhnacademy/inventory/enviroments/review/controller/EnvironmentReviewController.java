package com.nhnacademy.inventory.enviroments.review.controller;

import com.nhnacademy.inventory.enviroments.review.dto.InventoryReviewRequest;
import com.nhnacademy.inventory.enviroments.review.dto.ReviewHistoryDetailResponse;
import com.nhnacademy.inventory.enviroments.review.dto.ReviewHistorySummaryResponse;
import com.nhnacademy.inventory.enviroments.review.dto.UnderReviewInventoryResponse;
import com.nhnacademy.inventory.enviroments.review.service.EnvironmentReviewService;
import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class EnvironmentReviewController {
    private final EnvironmentReviewService environmentReviewService;

    @GetMapping("/inventories/under-reviews")
    public ResponseEntity<ApiResponse<PageResponse<UnderReviewInventoryResponse>>> getUnderReviewPage(
            @RequestParam(required = false) Long storageId,
            @PageableDefault(size = 20, sort = "lastReviewAt", direction = Sort.Direction.ASC) Pageable pageable
    ){
        Page<UnderReviewInventoryResponse> responsePage = environmentReviewService.getUnderReviewPage(storageId, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @PostMapping("/inventories/{inventory-id}/environment-reviews")
    public ResponseEntity<Void> reviewInventory(
            @RequestBody @Valid InventoryReviewRequest request,
            @PathVariable(name = "inventory-id") Long inventoryId
    ){
        environmentReviewService.reviewInventory(inventoryId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/environment-reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewHistorySummaryResponse>>> getReviewHistoryPage(
            @RequestParam(required = false) Long storageId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<ReviewHistorySummaryResponse> responsePage = environmentReviewService.getReviewHistoryPage(storageId, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @GetMapping("/environment-reviews/{environment-review-id}")
    public ResponseEntity<ApiResponse<ReviewHistoryDetailResponse>> getReviewDetail(
            @PathVariable(name = "environment-review-id") Long environmentReviewId
    ){
        ReviewHistoryDetailResponse response = environmentReviewService.getReviewDetail(environmentReviewId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
