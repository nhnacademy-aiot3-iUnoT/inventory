package com.nhnacademy.inventory.organizations.storage.dto;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;

import java.time.LocalDateTime;

public record StorageDetailResponse(
        Long storageId,
        Long organizationId,
        String organizationName,
        String name,
        String description,
        StorageStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StorageDetailResponse from(Storage storage){
        return new StorageDetailResponse(storage.getId(), storage.getOrganization().getId(),
                storage.getOrganization().getName(), storage.getName(), storage.getDescription(), storage.getStatus(),
                storage.getCreatedAt(), storage.getUpdatedAt());
    }
}