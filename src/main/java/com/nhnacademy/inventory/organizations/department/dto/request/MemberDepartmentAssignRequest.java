package com.nhnacademy.inventory.organizations.department.dto.request;

import java.util.List;

public record MemberDepartmentAssignRequest(
        List<Long> departmentIds
) {
}
