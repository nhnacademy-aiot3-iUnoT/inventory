package com.nhnacademy.inventory.inventories.threshold.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdSaveRequest;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdInfoResponse;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdUpdateRequest;
import com.nhnacademy.inventory.inventories.threshold.service.StockThresholdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class StockThresholdController {
    private final StockThresholdService stockThresholdService;

    @PostMapping("/storages/{storage-id}/stock-thresholds")
    public ResponseEntity<ApiResponse<StockThresholdInfoResponse>> saveStockThreshold(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid StockThresholdSaveRequest request
    ){
        StockThresholdInfoResponse response = stockThresholdService.saveStockThreshold(storageId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/storages/{storage-id}/stock-thresholds")
    public ResponseEntity<ApiResponse<List<StockThresholdInfoResponse>>> getStockThresholds(
            @PathVariable(name = "storage-id") Long storageId
    ){
        List<StockThresholdInfoResponse> responses = stockThresholdService.getStockThresholds(storageId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/storages/{storage-id}/stock-thresholds/{stock-threshold-id}")
    public ResponseEntity<ApiResponse<StockThresholdInfoResponse>> createStockThreshold(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "stock-threshold-id") Long stockThresholdId,
            @RequestBody @Valid StockThresholdUpdateRequest request
    ){
        StockThresholdInfoResponse response = stockThresholdService.updateStockThreshold(storageId, stockThresholdId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/storages/{storage-id}/stock-thresholds/{stock-threshold-id}")
    public ResponseEntity<ApiResponse<StockThresholdInfoResponse>> deleteStockThreshold(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "stock-threshold-id") Long stockThresholdId
    ){
        stockThresholdService.deleteStockThreshold(storageId, stockThresholdId);

        return ResponseEntity.noContent().build();
    }
}
