package com.nhnacademy.inventory.organizations.department.dto.response;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.DepartmentStatus;

import java.time.LocalDateTime;

public record DepartmentInfoResponse (
        Long id,
        String name,
        String description,
        DepartmentStatus status,
        LocalDateTime createdAt
) {
    public static DepartmentInfoResponse from(Department department) {
        return new DepartmentInfoResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getStatus(),
                department.getCreatedAt()
        );
    }
}
