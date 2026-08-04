package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
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
            @RequestHeader("X-User-Id") UUID accountUuid,
            @RequestBody @Valid ZoneCreateRequest request
    ){
        ZoneInfoResponse response = zoneService.createZone(storageId, accountUuid, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/storages/{storage-id}/zones")
    public ResponseEntity<ApiResponse<List<ZoneInfoResponse>>> getZones(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestHeader("X-User-Id") UUID accountUuid
    ){
        List<ZoneInfoResponse> responses = zoneService.getZones(storageId, accountUuid);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/storages/{storage-id}/zones/{zone-id}")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneInfo(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestHeader("X-User-Id") UUID accountUuid,
            @RequestBody @Valid ZoneUpdateRequest request
    ){
        ZoneInfoResponse response = zoneService.updateZone(storageId, zoneId, accountUuid, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/zones/{zone-id}/status")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneStatus(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestHeader("X-User-Id") UUID accountUuid,
            @RequestBody @Valid ZoneStatusUpdateRequest request
    ){
        ZoneInfoResponse response = zoneService.updateZoneStatus(storageId, zoneId, accountUuid, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storage-id}/zones/{zone-id}/env-status")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneEnvStatus(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestHeader("X-User-Id") UUID accountUuid,
            @RequestBody @Valid ZoneEnvStatusUpdateRequest request
    ){
        ZoneInfoResponse response = zoneService.updateZoneEnvStatus(storageId, zoneId, accountUuid, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/storages/{storage-id}/zones/{zone-id}")
    public ResponseEntity<Void> deleteZone(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestHeader("X-User-Id") UUID accountUuid
    ){
        zoneService.closeZone(storageId, zoneId, accountUuid);

        return ResponseEntity.noContent().build();
    }
}
