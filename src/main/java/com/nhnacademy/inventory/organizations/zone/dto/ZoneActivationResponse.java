package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;

public record ZoneActivationResponse(
        Long zoneId,
        boolean active,
        boolean zoneActive,
        boolean storageActive
) {
    public static ZoneActivationResponse from(Zone zone) {
        return new ZoneActivationResponse(
                zone.getId(),
                zone.isActive(),
                zone.getStatus() == ZoneStatus.ACTIVE,
                zone.getStorage().isActive()
        );
    }
}
