package com.nhnacademy.inventory.organizations.sensor.dto;

import com.nhnacademy.inventory.organizations.sensor.domain.ZoneSensor;

public record ZoneSensorInfoResponse(
        Long zoneSensorId,
        Long zoneId,
        String deviceEui,
        String name

) {
    public static ZoneSensorInfoResponse from(ZoneSensor zoneSensor) {
        return new ZoneSensorInfoResponse(
                zoneSensor.getId(),
                zoneSensor.getZone().getId(),
                zoneSensor.getDeviceEui(),
                zoneSensor.getName()
        );
    }
}
