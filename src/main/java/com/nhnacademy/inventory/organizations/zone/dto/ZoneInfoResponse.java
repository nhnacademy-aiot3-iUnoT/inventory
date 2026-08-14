package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;

import java.time.LocalDateTime;

public record ZoneInfoResponse (
        Long zoneId,
        Long storageId,
        String name,
        ZoneStatus status,
        EnvStatus envStatus
){
    public static ZoneInfoResponse from(Zone zone){
        return new ZoneInfoResponse(zone.getId(), zone.getStorage().getId(),
                zone.getName(), zone.getStatus(), zone.getEnvStatus());
    }
}
