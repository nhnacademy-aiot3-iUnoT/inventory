package com.nhnacademy.inventory.inventories.inventory.operation;

import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryOperationAccessValidator {

    private final StorageService storageService;
    private final OrganizationAccessService organizationAccessService;

    public void validate(Storage storage, Medicine medicine) {
        storageService.checkStoragePermission(storage.getId());

        if (medicine.requiresNarcoticHandlingPermission()) {
            organizationAccessService.requireOwnerOrBossOf(
                    storage.getOrganization().getId()
            );
        }
    }
}