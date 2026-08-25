package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.organizations.department.domain.DepartmentStatus;

public record DepartmentByStorageResponse(
        Long departmentId,
        String name,
        DepartmentStatus departmentStatus
) {
}
