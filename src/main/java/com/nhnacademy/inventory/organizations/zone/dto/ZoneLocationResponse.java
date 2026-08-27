package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.Zone;

public record ZoneLocationResponse(
        Long organizationId,
        Long storageId,
        Long zoneId
) {
    public static ZoneLocationResponse from(Zone zone) {
        return new ZoneLocationResponse(
                zone.getStorage().getOrganization().getId(),
                zone.getStorage().getId(),
                zone.getId()
        );
    }
}
