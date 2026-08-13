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
import java.util.UUID;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class ZoneController {
    private final ZoneService zoneService;

    @PostMapping("/storages/{storage-id}/zones")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> createZone(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestBody @Valid ZoneCreateRequest request
    ){
        ZoneInfoResponse response = zoneService.createZone(storageId, request);

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

    @PutMapping("/storages/{storage-id}/zones/{zone-id}")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneInfo(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ZoneUpdateRequest request
    ){
        ZoneInfoResponse response = zoneService.updateZone(storageId, zoneId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/zones/{zone-id}/status")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneStatus(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ZoneStatusUpdateRequest request
    ){
        ZoneInfoResponse response = zoneService.updateZoneStatus(storageId, zoneId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/zones/{zone-id}/env-status")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneEnvStatus(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ZoneEnvStatusUpdateRequest request
    ){
        ZoneInfoResponse response = zoneService.updateZoneEnvStatus(storageId, zoneId, request);

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

    @PutMapping("/internal/zones/{zone-id}/env-status")
    public ResponseEntity<Void> internalUpdateEnvStatus(
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestParam("env-status") EnvStatus envStatus
    ){
        zoneService.internalUpdateEnvStatus(zoneId, envStatus);

        return ResponseEntity.noContent().build();
    }
}
