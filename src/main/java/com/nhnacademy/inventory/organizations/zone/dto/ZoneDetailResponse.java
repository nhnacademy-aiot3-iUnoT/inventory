package com.nhnacademy.inventory.organizations.zone.dto;

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
        return new ZoneDetailResponse(
                zone.getId(),
                zone.getStorage().getId(),
                zone.getStorage().getOrganization().getName(),
                zone.getStorage().getName(),
                zone.getName(),
                zone.getDescription(),
                zone.getStatus(),
                zone.getEnvStatus(),
                zone.getCreatedAt(),
                zone.getUpdatedAt()
        );
    }
}