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

    @PostMapping("/storages/{storageId}/zones")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> createZone(
            @PathVariable Long storageId,
            @RequestBody @Valid ZoneCreateRequest request
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        ZoneInfoResponse response = zoneService.createZone(storageId, uuid, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/storages/{storageId}/zones")
    public ResponseEntity<ApiResponse<List<ZoneInfoResponse>>> getZones(
            @PathVariable Long storageId
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        List<ZoneInfoResponse> responses = zoneService.getZones(storageId, uuid);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/storages/{storageId}/zones/{zoneId}")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneInfo(
            @PathVariable Long storageId,
            @PathVariable Long zoneId,
            @RequestBody @Valid ZoneUpdateRequest request
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        ZoneInfoResponse response = zoneService.updateZone(storageId, zoneId, uuid, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storageId}/zones/{zoneId}/status")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneStatus(
            @PathVariable Long storageId,
            @PathVariable Long zoneId,
            @RequestBody @Valid ZoneStatusUpdateRequest request
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        ZoneInfoResponse response = zoneService.updateZoneStatus(storageId, zoneId, uuid, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/storages/{storageId}/zones/{zoneId}/env-status")
    public ResponseEntity<ApiResponse<ZoneInfoResponse>> updateZoneEnvStatus(
            @PathVariable Long storageId,
            @PathVariable Long zoneId,
            @RequestBody @Valid ZoneEnvStatusUpdateRequest request
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        ZoneInfoResponse response = zoneService.updateZoneEnvStatus(storageId, zoneId, uuid, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/storages/{storageId}/zones/{zoneId}")
    public ResponseEntity<Void> deleteZone(
            @PathVariable Long storageId,
            @PathVariable Long zoneId
    ){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        zoneService.closeZone(storageId, zoneId, uuid);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
