package com.nhnacademy.inventory.organizations.department.dto.request;

import com.nhnacademy.inventory.organizations.department.domain.DepartmentStatus;
import jakarta.validation.constraints.NotNull;

public record DepartmentStatusUpdateRequest (
        @NotNull(message = "변경하고자 하는 부서 상태를 입력해주세요")
        DepartmentStatus status
) {
}

