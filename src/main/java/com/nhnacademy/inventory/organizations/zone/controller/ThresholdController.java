package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdDetailResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSaveRequest;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSpecResponse;
import com.nhnacademy.inventory.organizations.zone.service.ThresholdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class ThresholdController {
    private final ThresholdService thresholdService;

    @PostMapping("/zones/{zone-id}/zone-thresholds")
    public ResponseEntity<ApiResponse<ThresholdDetailResponse>> saveZoneThreshold(
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ThresholdSaveRequest request
    ){
        ThresholdDetailResponse response = thresholdService.saveThreshold(zoneId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/zones/{zone-id}/zone-thresholds")
    public ResponseEntity<ApiResponse<List<ThresholdInfoResponse>>> getZoneThresholds(
            @PathVariable(name = "zone-id") Long zoneId
    ){
        List<ThresholdInfoResponse> responses = thresholdService.getThresholds(zoneId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/zones/{zone-id}/zone-thresholds/{zone-threshold-id}")
    public ResponseEntity<ApiResponse<ThresholdDetailResponse>> getZoneThreshold(
            @PathVariable(name = "zone-id") Long zoneId,
            @PathVariable(name = "zone-threshold-id") Long zoneThresholdId
    ){
        ThresholdDetailResponse response = thresholdService.getThreshold(zoneId, zoneThresholdId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/zones/{zone-id}/zone-thresholds/{zone-threshold-id}")
    public ResponseEntity<Void> deleteZoneThreshold(
            @PathVariable(name = "zone-id") Long zoneId,
            @PathVariable(name = "zone-threshold-id") Long zoneThresholdId
    ){
        thresholdService.deleteThreshold(zoneId, zoneThresholdId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/zones/{zone-id}/zone-threshold")
    public ResponseEntity<ApiResponse<List<ThresholdSpecResponse>>> internalGetThresholds(
            @PathVariable(name = "zone-id") Long zoneId
    ){
        List<ThresholdSpecResponse> responses = thresholdService.internalGetThresholds(zoneId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
