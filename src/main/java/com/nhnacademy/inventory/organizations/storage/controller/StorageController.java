package com.nhnacademy.inventory.organizations.storage.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.storage.dto.*;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
@Slf4j
public class StorageController {
    private final StorageService storageService;

    @PostMapping("/storages")
    public ResponseEntity<ApiResponse<StorageDetailResponse>> createStorage(
            @RequestBody @Valid StorageCreateRequest request
    ){
        StorageDetailResponse response = storageService.createStorage(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/storages")
    public ResponseEntity<ApiResponse<List<StorageInfoResponse>>> getStorages(@RequestParam(required = false) String name) {
        List<StorageInfoResponse> responses = name == null ? storageService.getStorages() : storageService.searchStorages(name);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/storages/{storage-id}")
    public ResponseEntity<ApiResponse<StorageDetailResponse>> getStorage(
            @PathVariable(name = "storage-id") Long storageId
    ){
        StorageDetailResponse response = storageService.getStorage(storageId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/inbound/storages")
    public ResponseEntity<ApiResponse<List<StorageInfoResponse>>> getStorageInbound(){


        List<StorageInfoResponse> responses = storageService.getStoragesInbound();
        log.info("storage responses count: {}",responses);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }


    @PutMapping("/storages/{storage-id}")
    public ResponseEntity<ApiResponse<StorageDetailResponse>> updateStorage(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid StorageUpdateRequest request
    ){
        StorageDetailResponse response = storageService.updateStorage(storageId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/status")
    public ResponseEntity<ApiResponse<StorageDetailResponse>> updateStorageStatus(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid StorageStatusUpdateRequest request
    ){
        StorageDetailResponse response = storageService.updateStorageStatus(storageId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/storages/{storage-id}")
    public ResponseEntity<Void> deleteStorage(
            @PathVariable(name = "storage-id") Long storageId
    ){
        storageService.closeStorage(storageId);

        return ResponseEntity.noContent().build();
    }
}
