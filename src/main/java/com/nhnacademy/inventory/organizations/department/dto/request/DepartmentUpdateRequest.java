package com.nhnacademy.inventory.organizations.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentUpdateRequest (
        @NotBlank(message = "부서 이름은 필수 입력입니다.")
        @Size(message = "부서 이름은 최대 30자입니다.")
        String name,

        @Size(message = "부서 설명은 최대 255자 입니다.")
        String description
) {
}
