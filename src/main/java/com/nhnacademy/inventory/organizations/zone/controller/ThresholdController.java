package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSaveRequest;
import com.nhnacademy.inventory.organizations.zone.service.ThresholdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class ThresholdController {
    private final ThresholdService thresholdService;

    @PutMapping("/zones/{zoneId}/zone-threshold")
    public ResponseEntity<ApiResponse<ThresholdInfoResponse>> saveZoneThreshold(
            @PathVariable Long zoneId,
            @RequestHeader("X-User-Id") UUID accountUuid,
            @RequestBody @Valid ThresholdSaveRequest request
    ){
        ThresholdInfoResponse response = thresholdService.saveThreshold(zoneId, accountUuid, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/zones/{zoneId}/zone-threshold")
    public ResponseEntity<ApiResponse<List<ThresholdInfoResponse>>> getZoneThreshold(
            @PathVariable Long zoneId,
            @RequestHeader("X-User-Id") UUID accountUuid
    ){
        List<ThresholdInfoResponse> responses = thresholdService.getThresholds(zoneId, accountUuid);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/zones/{zoneId}/zone-threshold/{zoneThresholdId}")
    public ResponseEntity<Void> deleteZoneThreshold(
            @PathVariable Long zoneId,
            @PathVariable Long zoneThresholdId,
            @RequestHeader("X-User-Id") UUID accountUuid
    ){
        thresholdService.deleteThreshold(zoneId, zoneThresholdId, accountUuid);

        return ResponseEntity.noContent().build();
    }
}
