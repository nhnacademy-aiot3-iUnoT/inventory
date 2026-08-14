package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;

import java.math.BigDecimal;

public record ThresholdDetailResponse(
        Long zoneThresholdId,
        Long storageId,
        Long zoneId,
        Long sensorTypeId,
        String organizationName,
        String storageName,
        String zoneName,
        String sensorTypeName,
        String sensorTypeDescription,
        BigDecimal minValue,
        BigDecimal maxValue,
        Integer alertDuration
){
    public static ThresholdDetailResponse from(ZoneThreshold threshold){
        return new ThresholdDetailResponse(
                threshold.getZoneThresholdId(),
                threshold.getZone().getStorage().getId(),
                threshold.getZone().getId(),
                threshold.getSensorType().getSensorTypeId(),
                threshold.getZone().getStorage().getOrganization().getName(),
                threshold.getZone().getStorage().getName(),
                threshold.getZone().getName(),
                threshold.getSensorType().getName(),
                threshold.getSensorType().getDescription(),
                threshold.getMinValue(),
                threshold.getMaxValue(),
                threshold.getAlertDuration()
        );
    }
}
