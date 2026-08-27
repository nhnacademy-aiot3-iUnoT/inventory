package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.dto.*;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class ZoneController {
    private final ZoneService zoneService;

    @PostMapping("/storages/{storage-id}/zones")
    public ResponseEntity<ApiResponse<ZoneDetailResponse>> createZone(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid ZoneCreateRequest request
    ){
        ZoneDetailResponse response = zoneService.createZone(storageId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/storages/{storage-id}/zones")
    public ResponseEntity<ApiResponse<List<ZoneInfoResponse>>> getZones(
            @PathVariable(name = "storage-id") Long storageId
    ){
        List<ZoneInfoResponse> responses = zoneService.getZones(storageId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/storages/{storage-id}/zones/{zone-id}")
    public ResponseEntity<ApiResponse<ZoneDetailResponse>> getZone(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId
    ){
        ZoneDetailResponse response = zoneService.getZone(storageId, zoneId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/zones/{zone-id}")
    public ResponseEntity<ApiResponse<ZoneDetailResponse>> updateZoneInfo(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ZoneUpdateRequest request
    ){
        ZoneDetailResponse response = zoneService.updateZone(storageId, zoneId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/zones/{zone-id}/status")
    public ResponseEntity<ApiResponse<ZoneDetailResponse>> updateZoneStatus(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ZoneStatusUpdateRequest request
    ){
        ZoneDetailResponse response = zoneService.updateZoneStatus(storageId, zoneId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/zones/{zone-id}/env-status")
    public ResponseEntity<ApiResponse<ZoneDetailResponse>> updateZoneEnvStatus(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ZoneEnvStatusUpdateRequest request
    ){
        ZoneDetailResponse response = zoneService.updateZoneEnvStatus(storageId, zoneId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/storages/{storage-id}/zones/{zone-id}")
    public ResponseEntity<Void> deleteZone(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId
    ){
        zoneService.closeZone(storageId, zoneId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/zones/{zone-id}/location")
    public ResponseEntity<ApiResponse<ZoneLocationResponse>> internalGetZoneLocation(
            @PathVariable(name = "zone-id") Long zoneId
    ){
        ZoneLocationResponse response = zoneService.getZoneLocation(zoneId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/internal/zones/{zone-id}/env-status")
    public ResponseEntity<Void> internalUpdateEnvStatus(
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestParam("env-status") EnvStatus envStatus
    ){
        zoneService.internalUpdateEnvStatus(zoneId, envStatus);

        return ResponseEntity.noContent().build();
    }
}
