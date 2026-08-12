package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSaveRequest;
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

    @PostMapping("/zones/{zone-id}/zone-threshold")
    public ResponseEntity<ApiResponse<ThresholdInfoResponse>> saveZoneThreshold(
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ThresholdSaveRequest request
    ){
        ThresholdInfoResponse response = thresholdService.saveThreshold(zoneId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/zones/{zone-id}/zone-threshold")
    public ResponseEntity<ApiResponse<List<ThresholdInfoResponse>>> getZoneThreshold(
            @PathVariable(name = "zone-id") Long zoneId
    ){
        List<ThresholdInfoResponse> responses = thresholdService.getThresholds(zoneId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/zones/{zone-id}/zone-threshold/{zone-threshold-id}")
    public ResponseEntity<Void> deleteZoneThreshold(
            @PathVariable(name = "zone-id") Long zoneId,
            @PathVariable(name = "zone-threshold-id") Long zoneThresholdId
    ){
        thresholdService.deleteThreshold(zoneId, zoneThresholdId);

        return ResponseEntity.noContent().build();
    }
}
