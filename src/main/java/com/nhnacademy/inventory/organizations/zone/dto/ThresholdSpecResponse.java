package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;

import java.math.BigDecimal;

public record ThresholdSpecResponse(
        Long zoneId,
        Long sensorTypeId,
        String sensorTypeName,
        BigDecimal minValue,
        BigDecimal maxValue,
        Integer alertDuration
) {
    public static ThresholdSpecResponse from(ZoneThreshold threshold){
        return new ThresholdSpecResponse(
                threshold.getZone().getId(),
                threshold.getSensorType().getSensorTypeId(),
                threshold.getSensorType().getName(),
                threshold.getMinValue(),
                threshold.getMaxValue(),
                threshold.getAlertDuration()
        );
    }
}
