package com.nhnacademy.inventory.dashboards.dto;

import java.util.List;

/**
 * 부서 선택 드롭다운. 권한에 따라 보여줄 목록이 달라진다.
 * - 일반 조직원 : myDepartments 만 채워진다
 * - 관리자      : 조직 전체 합산과 otherDepartments 까지 고를 수 있다
 */
public record DashboardDepartmentsResponse(
        boolean orgAdmin,
        String organizationName,
        List<DepartmentOptionResponse> myDepartments,
        List<DepartmentOptionResponse> otherDepartments
) {
    public record DepartmentOptionResponse(
            Long departmentId,
            String name
    ) {
    }
}
