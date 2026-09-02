package com.nhnacademy.inventory.dashboards.dto;

import java.util.List;

/**
 * 부서 선택 드롭다운.
 * - 일반 조직원 : 자기 소속 부서만
 * - 관리자      : 조직의 모든 부서 + 조직 전체 합산(orgAdmin 이면 고를 수 있다)
 */
public record DashboardDepartmentsResponse(
        boolean orgAdmin,
        String organizationName,
        List<DepartmentOptionResponse> departments
) {
    public record DepartmentOptionResponse(
            Long departmentId,
            String name
    ) {
    }
}
