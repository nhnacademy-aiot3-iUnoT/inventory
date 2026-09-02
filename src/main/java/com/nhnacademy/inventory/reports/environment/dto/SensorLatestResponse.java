package com.nhnacademy.inventory.reports.environment.dto;

/**
 * 룰엔진의 센서 최신값 응답
 */
public record SensorLatestResponse(
        Long organizationId,
        String deviceEui,
        Long storageId,
        Long zoneId,
        String sensorType,
        Double value,
        String unit,
        String measuredAt
) {
}
