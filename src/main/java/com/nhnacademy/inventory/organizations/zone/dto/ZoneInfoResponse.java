package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;


public record ZoneInfoResponse (
        Long zoneId,
        Long storageId,
        String name,
        ZoneStatus status,
        EnvStatus envStatus
){
    public static ZoneInfoResponse from(Zone zone){
        ZoneStatus zoneStatus;

        if(zone.getStorage().getStatus() == StorageStatus.ACTIVE){
            zoneStatus = zone.getStatus();
        }else{
            zoneStatus = ZoneStatus.INACTIVE;
        }

        return new ZoneInfoResponse(
                zone.getId(),
                zone.getStorage().getId(),
                zone.getName(),
                zoneStatus,
                zone.getEnvStatus());
    }
}
