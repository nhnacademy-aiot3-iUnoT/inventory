package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.global.client.AccountClient;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.dto.request.MemberDepartmentAssignRequest;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberResponse;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberDepartmentService {

    private final MemberDepartmentRepository memberDepartmentRepository;
    private final OrganizationMemberService orgMemberService;
    private final OrganizationAccessService orgAccessService;
    private final DepartmentRepository departmentRepository;
    private final AccountClient accountClient;

    /**
     * 부서에 속한 조직원 목록
     */
    public List<OrganizationMemberResponse> getDepartmentMembers(Long departmentId) {
        Organization organization = orgAccessService.getCurrentMember().getOrganization();
        validateDepartment(departmentId, organization.getId());

        List<OrganizationMember> members = memberDepartmentRepository.findAllWithMemberByDepartmentId(departmentId)
                .stream()
                .map(MemberDepartment::getOrganizationMember)
                .toList();

        Map<UUID, String> emails = getEmailMap(members);

        return members.stream()
                .map(member -> new OrganizationMemberResponse (
                        member.getId(),
                        emails.get(member.getAccountUuid()),
                        member.getOrganizationRole(),
                        member.getJoinedAt())
                ).toList();
    }

    @Transactional
    public void addMember(Long departmentId, Long memberId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();
        Department department = validateDepartment(departmentId, organization.getId());

        OrganizationMember member = orgMemberService.getMemberById(memberId, organization.getId());

        if (!memberDepartmentRepository.existsByDepartmentIdAndOrganizationMemberId(departmentId, memberId)) {
            memberDepartmentRepository.save(MemberDepartment.create(member, department));
        }

    }

    @Transactional
    public void removeMember(Long departmentId, Long memberId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();
        validateDepartment(departmentId, organization.getId());

        OrganizationMember member = orgMemberService.getMemberById(memberId, organization.getId());
        memberDepartmentRepository.deleteByDepartmentIdAndOrganizationMemberId(departmentId, member.getId());
    }

    public List<DepartmentListResponse> getMyDepartments() {
        OrganizationMember member = orgAccessService.getCurrentMember();

        return memberDepartmentRepository
                .findAllWithDepartmentByMemberId(member.getId())
                .stream()
                .map(MemberDepartment::getDepartment)
                .map(DepartmentListResponse::from)
                .toList();
    }

/**
     * 지금 로그인한 조직원이 그 부서에 소속되어 있는지 확인한다.
     */
    public boolean isMyDepartment(Long departmentId) {
        OrganizationMember member = orgAccessService.getCurrentMember();

        return memberDepartmentRepository
                .existsByDepartmentIdAndOrganizationMemberId(departmentId, member.getId());
    }

    /**
     * 소속 부서를 통해 접근할 수 있는 저장소 ID 목록.
     */
    public List<Long> getAccessibleStorageIds() {
        OrganizationMember member = orgAccessService.getCurrentMember();

        return memberDepartmentRepository.findAccessibleStorageIds(member.getId());
    }

    @Transactional
    public void assignMemberDepartments(Long memberId, MemberDepartmentAssignRequest request) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();
        OrganizationMember member = orgMemberService.getMemberById(memberId, organization.getId());

        List<Long> departmentIds = request.departmentIds() == null
                ? List.of()
                : request.departmentIds().stream().distinct().toList();

        List<Department> departments = departmentRepository.findAllByIdInAndOrganizationId(
                departmentIds, organization.getId());

        if (departments.size() != departmentIds.size()) {
            throw new DepartmentNotFoundException();
        }

        memberDepartmentRepository.deleteByOrganizationMemberId(memberId);
        memberDepartmentRepository.saveAll(departments.stream()
                .map(department -> MemberDepartment.create(member, department))
                .toList());
    }

    private Department validateDepartment(Long departmentId, Long organizationId) {
        return departmentRepository.findByIdAndOrganizationId(departmentId, organizationId)
                .orElseThrow(DepartmentNotFoundException::new);
    }

    private Map<UUID, String> getEmailMap(List<OrganizationMember> members) {
        if (members.isEmpty()) {
            return Map.of();
        }

        return accountClient.findByUuids(members.stream()
                        .map(OrganizationMember::getAccountUuid)
                        .toList())
                .stream()
                .collect(Collectors.toMap(AccountResponse::accountUuid, AccountResponse::email));
    }
}
