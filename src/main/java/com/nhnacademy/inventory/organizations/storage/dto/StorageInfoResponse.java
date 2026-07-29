package com.nhnacademy.inventory.organizations.storage.dto;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;

import java.time.LocalDateTime;

public record StorageInfoResponse(
        Long storageId,
        Long organizationId,
        String name,
        String description,
        StorageStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StorageInfoResponse from(Storage storage){
        return new StorageInfoResponse(storage.getId(), storage.getOrganization().getId(),
                storage.getName(), storage.getDescription(), storage.getStatus(),
                storage.getCreatedAt(), storage.getUpdatedAt());
    }
}
