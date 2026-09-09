package com.nhnacademy.inventory.inventories.inventory.operation;

import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryOperationAccessValidator {

    private final StorageService storageService;
    private final ZoneService zoneService;
    private final OrganizationAccessService organizationAccessService;

    public void validate(Zone zone, Medicine medicine) {
        storageService.checkStoragePermission(zone.getStorage().getId());
        zoneService.validateZoneStatus(zone);

        if (medicine.requiresNarcoticHandlingPermission()) {
            organizationAccessService.requireOwnerOrBossOf(
                    zone.getStorage().getOrganization().getId()
            );
        }
    }
}