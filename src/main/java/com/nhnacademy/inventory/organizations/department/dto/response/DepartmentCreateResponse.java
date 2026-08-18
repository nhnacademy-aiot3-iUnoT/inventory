package com.nhnacademy.inventory.organizations.department.dto.response;

import com.nhnacademy.inventory.organizations.department.domain.Department;

public record DepartmentCreateResponse (
        Long id,
        String name
) {
    public static DepartmentCreateResponse from(Department department) {
        return new DepartmentCreateResponse (
                department.getId(),
                department.getName()
       );
    }
}
