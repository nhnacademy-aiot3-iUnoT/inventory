package com.nhnacademy.inventory.organizations.storage.dto;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;

import java.time.LocalDateTime;

public record StorageInfoResponse(
        Long storageId,
        Long organizationId,
        String organizationName,
        String name,
        StorageStatus status
) {
    public static StorageInfoResponse from(Storage storage){
        return new StorageInfoResponse(storage.getId(), storage.getOrganization().getId(),
                storage.getOrganization().getName(), storage.getName(), storage.getStatus());
    }
}
