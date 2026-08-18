package com.nhnacademy.inventory.organizations.sensor.dto;

import com.nhnacademy.inventory.organizations.sensor.domain.ZoneSensor;

public record ZoneSensorDetailResponse(
        Long zoneSensorId,
        Long storageId,
        Long zoneId,
        String deviceEui,
        String organizationName,
        String storageName,
        String zoneName,
        String name,
        String description

) {
    public static ZoneSensorDetailResponse from(ZoneSensor zoneSensor) {
        return new ZoneSensorDetailResponse(
                zoneSensor.getId(),
                zoneSensor.getZone().getStorage().getId(),
                zoneSensor.getZone().getId(),
                zoneSensor.getDeviceEui(),
                zoneSensor.getZone().getStorage().getOrganization().getName(),
                zoneSensor.getZone().getStorage().getName(),
                zoneSensor.getZone().getName(),
                zoneSensor.getName(),
                zoneSensor.getDescription()
        );
    }
}