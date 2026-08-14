package com.nhnacademy.inventory.organizations.sensor.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.sensor.dto.*;
import com.nhnacademy.inventory.organizations.sensor.service.ZoneSensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class ZoneSensorController {
    private final ZoneSensorService zoneSensorService;

    @PostMapping("/zones/{zone-id}/zone-sensors")
    public ResponseEntity<ApiResponse<ZoneSensorDetailResponse>> createZoneSensor(
            @PathVariable(name = "zone-id") Long zoneId,
            @RequestBody @Valid ZoneSensorCreateRequest request
    ){
        ZoneSensorDetailResponse response = zoneSensorService.createZoneSensor(zoneId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/zones/{zone-id}/zone-sensors")
    public ResponseEntity<ApiResponse<List<ZoneSensorInfoResponse>>> getZoneSensors(
            @PathVariable(name = "zone-id") Long zoneId
    ){
        List<ZoneSensorInfoResponse> responses = zoneSensorService.getZoneSensors(zoneId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/zones/{zone-id}/zone-sensors/{zone-sensor-id}")
    public ResponseEntity<ApiResponse<ZoneSensorDetailResponse>> getZoneSensor(
            @PathVariable(name = "zone-id") Long zoneId,
            @PathVariable(name = "zone-sensor-id") Long zoneSensorId
    ){
        ZoneSensorDetailResponse response = zoneSensorService.getZoneSensor(zoneId, zoneSensorId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/zones/{zone-id}/zone-sensors/{zone-sensor-id}")
    public ResponseEntity<ApiResponse<ZoneSensorDetailResponse>> updateZoneSensorInfo(
            @PathVariable(name = "zone-id") Long zoneId,
            @PathVariable(name = "zone-sensor-id") Long zoneSensorId,
            @RequestBody @Valid ZoneSensorUpdateRequest request
    ){
        ZoneSensorDetailResponse response = zoneSensorService.updateZoneSensorInfo(zoneId, zoneSensorId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/zones/{zone-id}/zone-sensors/{zone-sensor-id}")
    public ResponseEntity<Void> deleteZoneSensor(
            @PathVariable(name = "zone-id") Long zoneId,
            @PathVariable(name = "zone-sensor-id") Long zoneSensorId
    ){
        zoneSensorService.deleteZoneSensor(zoneId, zoneSensorId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/devices/location")
    public ResponseEntity<ApiResponse<DeviceLocationResponse>> getDeviceLocation(
            @RequestParam("device-eui") String deviceEui
    ){
        DeviceLocationResponse response = zoneSensorService.getDeviceLocation(deviceEui);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
