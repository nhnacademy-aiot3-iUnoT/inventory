package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;

public record SensorTypeInfoResponse(
        Long sensorTypeId,
        String name,
        String description
) {
    public static SensorTypeInfoResponse from(SensorType sensorType){
        return new SensorTypeInfoResponse(
                sensorType.getSensorTypeId(),
                sensorType.getName(),
                sensorType.getDescription()
        );
    }
}
