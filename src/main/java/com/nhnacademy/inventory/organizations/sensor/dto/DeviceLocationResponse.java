package com.nhnacademy.inventory.organizations.sensor.dto;

import com.nhnacademy.inventory.organizations.sensor.domain.ZoneSensor;

public record DeviceLocationResponse(
        Long organizationId,
        Long storageId,
        Long zoneId,
        String deviceEui
) {
    public static DeviceLocationResponse from(ZoneSensor zoneSensor){
        return new DeviceLocationResponse(
                zoneSensor.getZone().getStorage().getOrganization().getId(),
                zoneSensor.getZone().getStorage().getId(),
                zoneSensor.getZone().getId(),
                zoneSensor.getDeviceEui()
        );
    }
}
