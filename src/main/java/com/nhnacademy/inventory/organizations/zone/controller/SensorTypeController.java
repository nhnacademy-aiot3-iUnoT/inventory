package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeCreateRequest;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeInfoResponse;
import com.nhnacademy.inventory.organizations.zone.service.SensorTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class SensorTypeController {
    private final SensorTypeService sensorTypeService;

    @PostMapping("/sensor-types")
    public ResponseEntity<ApiResponse<SensorTypeInfoResponse>> createSensorType(
            @RequestBody @Valid SensorTypeCreateRequest request
            ){

        SensorTypeInfoResponse response = sensorTypeService.createSensorType(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/sensor-types")
    public ResponseEntity<ApiResponse<List<SensorTypeInfoResponse>>> getSensorType(){
        List<SensorTypeInfoResponse> responses = sensorTypeService.getSensorTypes();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/sensor-types/{sensorTypeId}")
    public ResponseEntity<Void> deleteSensorType(
            @PathVariable Long sensorTypeId
    ){
        sensorTypeService.deleteSensorType(sensorTypeId);

        return ResponseEntity.noContent().build();
    }
}
