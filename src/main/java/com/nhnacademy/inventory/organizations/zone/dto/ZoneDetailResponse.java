package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;

import java.time.LocalDateTime;

public record ZoneDetailResponse (
    Long zoneId,
    Long storageId,
    String organizationName,
    String storageName,
    String name,
    String description,
    ZoneStatus status,
    EnvStatus envStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ZoneDetailResponse from(Zone zone) {
        ZoneStatus zoneStatus;

        if(zone.getStorage().getStatus() == StorageStatus.ACTIVE){
            zoneStatus = zone.getStatus();
        }else{
            zoneStatus = ZoneStatus.INACTIVE;
        }

        return new ZoneDetailResponse(
                zone.getId(),
                zone.getStorage().getId(),
                zone.getStorage().getOrganization().getName(),
                zone.getStorage().getName(),
                zone.getName(),
                zone.getDescription(),
                zoneStatus,
                zone.getEnvStatus(),
                zone.getCreatedAt(),
                zone.getUpdatedAt()
        );
    }
}