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
import java.util.UUID;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping("/organizations/{organizationId}/storages")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> createStorage(
            @PathVariable Long organizationId,
            @RequestBody @Valid StorageCreateRequest request
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        StorageInfoResponse response = storageService.createStorage(organizationId, uuid, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/organizations/{organizationId}/storages")
    public ResponseEntity<ApiResponse<List<StorageInfoResponse>>> getStorages(
            @PathVariable Long organizationId
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        List<StorageInfoResponse> responses = storageService.getStorages(organizationId, uuid);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/organizations/{organizationId}/storages/{storageId}")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> updateStorage(
            @PathVariable Long organizationId,
            @PathVariable Long storageId,
            @RequestBody @Valid StorageUpdateRequest request
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        StorageInfoResponse response = storageService.updateStorage(organizationId, storageId, uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/organizations/{organizationId}/storages/{storageId}/status")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> updateStorageStatus(
            @PathVariable Long organizationId,
            @PathVariable Long storageId,
            @RequestBody @Valid StorageStatusUpdateRequest request
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        StorageInfoResponse response = storageService.updateStorageStatus(organizationId, storageId, uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/organizations/{organizationId}/storages/{storageId}")
    public ResponseEntity<Void> deleteStorage(
            @PathVariable Long organizationId,
            @PathVariable Long storageId
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        storageService.closeStorage(organizationId, storageId, uuid);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
