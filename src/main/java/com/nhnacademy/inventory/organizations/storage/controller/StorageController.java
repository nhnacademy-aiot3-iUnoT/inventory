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

    @PostMapping("/organizations/{organization-id}/storages")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> createStorage(
            @PathVariable(name = "organization-id") Long organizationId,
            @RequestBody @Valid StorageCreateRequest request
    ){

        StorageInfoResponse response = storageService.createStorage(organizationId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/organizations/{organization-id}/storages")
    public ResponseEntity<ApiResponse<List<StorageInfoResponse>>> getStorages(
            @PathVariable(name = "organization-id") Long organizationId
    ){

        List<StorageInfoResponse> responses = storageService.getStorages(organizationId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/organizations/{organization-id}/storages/{storage-id}")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> updateStorage(
            @PathVariable(name = "organization-id") Long organizationId,
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid StorageUpdateRequest request
    ){

        StorageInfoResponse response = storageService.updateStorage(organizationId, storageId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/organizations/{organization-id}/storages/{storage-id}/status")
    public ResponseEntity<ApiResponse<StorageInfoResponse>> updateStorageStatus(
            @PathVariable(name = "organization-id") Long organizationId,
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid StorageStatusUpdateRequest request
    ){

        StorageInfoResponse response = storageService.updateStorageStatus(organizationId, storageId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/organizations/{organization-id}/storages/{storage-id}")
    public ResponseEntity<Void> deleteStorage(
            @PathVariable(name = "organization-id") Long organizationId,
            @PathVariable(name = "storage-id") Long storageId
    ){

        storageService.closeStorage(organizationId, storageId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
