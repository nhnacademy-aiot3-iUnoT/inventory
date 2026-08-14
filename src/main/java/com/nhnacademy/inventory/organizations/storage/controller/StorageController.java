package com.nhnacademy.inventory.organizations.storage.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.storage.dto.StorageCreateRequest;
import com.nhnacademy.inventory.organizations.storage.dto.StorageInfoResponse;
import com.nhnacademy.inventory.organizations.storage.dto.StorageStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.storage.dto.StorageUpdateRequest;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping("/storages")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> createStorage(
            @RequestBody @Valid StorageCreateRequest request
    ){

        StorageInfoResponse response = storageService.createStorage(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/storages")
    public ResponseEntity<ApiResponse<List<StorageInfoResponse>>> getStorages(){

        List<StorageInfoResponse> responses = storageService.getStorages();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/storages/{storage-id}")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> getStorage(
            @PathVariable(name = "storage-id") Long storageId
    ){

        StorageInfoResponse response = storageService.getStorage(storageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> updateStorage(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid StorageUpdateRequest request
    ){

        StorageInfoResponse response = storageService.updateStorage(storageId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/status")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> updateStorageStatus(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid StorageStatusUpdateRequest request
    ){

        StorageInfoResponse response = storageService.updateStorageStatus(storageId, request);
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
