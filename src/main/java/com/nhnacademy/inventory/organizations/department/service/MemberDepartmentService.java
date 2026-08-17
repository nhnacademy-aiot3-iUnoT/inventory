package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.dto.request.MemberDepartmentUpdateRequest;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberDepartmentService {

    private final MemberDepartmentRepository memberDepartmentRepository;
    private final OrganizationMemberService orgMemberService;
    private final OrganizationAccessService orgAccessService;
    private final DepartmentRepository departmentRepository;

    public List<DepartmentListResponse> getMyDepartments() {
        OrganizationMember member = orgAccessService.getCurrentMember();

        return memberDepartmentRepository
                .findAllByOrganizationMemberId(member.getId())
                .stream()
                .map(MemberDepartment::getDepartment)
                .map(DepartmentListResponse::from)
                .toList();
    }

    public List<DepartmentListResponse> getMemberDepartments(Long memberId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();
        OrganizationMember member = orgMemberService.getMemberById(memberId, organization.getId());

        return memberDepartmentRepository
                .findAllByOrganizationMemberId(member.getId())
                .stream()
                .map(MemberDepartment::getDepartment)
                .map(DepartmentListResponse::from)
                .toList();
    }

    @Transactional
    public void updateMemberDepartments(Long memberId, MemberDepartmentUpdateRequest request) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();
        OrganizationMember member = orgMemberService.getMemberById(memberId, organization.getId());

        // 사용자가 선택한 부서 ID 목록
        Set<Long> requestedDepartmentIds = request.departmentIds() == null ? Set.of() : new HashSet<>(request.departmentIds());

        List<Department> requestedDepartments = requestedDepartmentIds.isEmpty() ? List.of()
                : departmentRepository.findAllByIdInAndOrganizationId(List.copyOf(requestedDepartmentIds), organization.getId());

        // 조직에 존재하지 않는 부서가 포함되어 있음
        if (requestedDepartments.size() != requestedDepartmentIds.size()) {
            throw new DepartmentNotFoundException();
        }

        List<MemberDepartment> currentAssignments = memberDepartmentRepository.findAllByOrganizationMemberId(member.getId());
        // 기존 배정 부서 중 선택에서 제외된 부서 연결 제거
        memberDepartmentRepository.deleteAll(currentAssignments.stream()
                .filter(assignment -> !requestedDepartmentIds.contains(assignment.getDepartment().getId()))
                .toList());

        Set<Long> existingDepartmentIds = currentAssignments.stream()
                .map(assignment -> assignment.getDepartment().getId())
                .collect(Collectors.toSet());

        // 새로 선택된 부서만 연결 생성
        memberDepartmentRepository.saveAll(requestedDepartments.stream()
                .filter(department -> !existingDepartmentIds.contains(department.getId()))
                .map(department -> MemberDepartment.create(member, department))
                .toList());
    }
}
