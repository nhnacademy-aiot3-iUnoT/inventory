package com.nhnacademy.inventory.organizations.department.dto.response;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.DepartmentStatus;

public record DepartmentListResponse(
        Long id,
        String name,
        DepartmentStatus status
) {
    public static DepartmentListResponse from(Department department) {
        return new DepartmentListResponse(
                department.getId(),
                department.getName(),
                department.getStatus()
        );
    }
}
