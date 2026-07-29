package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;

import java.math.BigDecimal;

public record ThresholdInfoResponse(
        Long zoneThresholdId,
        Long zoneId,
        Long sensorTypeId,
        BigDecimal minValue,
        BigDecimal maxValue,
        Integer alertDuration
){
    public static ThresholdInfoResponse from(ZoneThreshold threshold){
        return new ThresholdInfoResponse(
                threshold.getZoneThresholdId(),
                threshold.getZone().getId(),
                threshold.getSensorType().getSensorTypeId(),
                threshold.getMinValue(),
                threshold.getMaxValue(),
                threshold.getAlertDuration()
        );
    }
}
