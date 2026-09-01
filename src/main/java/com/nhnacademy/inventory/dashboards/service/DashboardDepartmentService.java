package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardDepartmentsResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardDepartmentsResponse.DepartmentOptionResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.service.DepartmentService;
import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 부서 선택 드롭다운 데이터를 한 번에 만들어 준다.
 * 새 쿼리 없이 기존 서비스 두 개를 권한으로 분기해 조합한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardDepartmentService {

    private final DashboardScopeResolver scopeResolver;
    private final MemberDepartmentService memberDepartmentService;
    private final DepartmentService departmentService;

    public DashboardDepartmentsResponse getDepartmentOptions() {
        OrganizationMember member = scopeResolver.getCurrentMember();
        boolean orgAdmin = member.isOwner() || member.isBoss();

        List<DepartmentListResponse> mine = memberDepartmentService.getMyDepartments();
        Set<Long> myIds = mine.stream()
                .map(DepartmentListResponse::id)
                .collect(Collectors.toSet());

        List<DepartmentListResponse> others = orgAdmin
                ? departmentService.getDepartments().stream()
                        .filter(department -> !myIds.contains(department.id()))
                        .toList()
                : List.of();

        return new DashboardDepartmentsResponse(
                orgAdmin,
                member.getOrganization().getName(),
                toOptions(mine),
                toOptions(others)
        );
    }

    private List<DepartmentOptionResponse> toOptions(List<DepartmentListResponse> departments) {
        return departments.stream()
                .map(department -> new DepartmentOptionResponse(department.id(), department.name()))
                .toList();
    }
}
