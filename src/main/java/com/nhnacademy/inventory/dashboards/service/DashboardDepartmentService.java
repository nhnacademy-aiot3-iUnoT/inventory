package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardDepartmentsResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardDepartmentsResponse.DepartmentOptionResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.service.DepartmentService;
import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardDepartmentService {

    private final OrganizationAccessService orgAccessService;
    private final MemberDepartmentService memberDepartmentService;
    private final DepartmentService departmentService;

    // 선택가능한 부서 목록 조회
    public DashboardDepartmentsResponse getDepartmentOptions() {
        OrganizationMember member = orgAccessService.getCurrentMember();
        boolean orgAdmin = member.isOwner() || member.isBoss();

        // 관리자면 전체 부서 조회, 일반 사용자면 소속된 부서만 조회
        List<DepartmentListResponse> departments = orgAdmin
                ? departmentService.getDepartments()
                : memberDepartmentService.getMyDepartments();

        return new DashboardDepartmentsResponse(
                orgAdmin,
                member.getOrganization().getName(),
                departments.stream()
                        .map(department -> new DepartmentOptionResponse(department.id(), department.name()))
                        .toList()
        );
    }
}
