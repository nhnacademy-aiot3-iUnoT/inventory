package com.nhnacademy.inventory.organizations.department.dto.response;

import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;

public record StorageByDepartmentResponse(
        Long storageId,
        String name,
        StorageStatus storageStatus
){
}
