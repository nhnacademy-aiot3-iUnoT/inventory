package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.exception.MemberDepartmentAlreadyExistsException;
import com.nhnacademy.inventory.organizations.department.exception.MemberDepartmentNotInException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberDepartmentService {

    private final MemberDepartmentRepository memberDepartmentRepository;
    private final OrganizationMemberService orgMemberService;
    private final OrganizationService organizationService;
    private final DepartmentRepository departmentRepository;

    public List<DepartmentListResponse> getMyDepartments() {
        OrganizationMember member =
                orgMemberService.getCurrentOrganizationMember(UserContext.getUserUuid());

        return memberDepartmentRepository
                .findAllByOrganizationMemberId(member.getId())
                .stream()
                .map(MemberDepartment::getDepartment)
                .map(DepartmentListResponse::from)
                .toList();
    }

    /**
     * 조직원에게 부서 지정
     */
    @Transactional
    public void assignDepartment(Long memberId, Long departmentId) {
        // boss/owner의 조직
        Organization organization = organizationService.getOrgAfterValidateOwnerOrBoss();

        if(memberDepartmentRepository.existsByOrganizationMemberIdAndDepartmentId(memberId, departmentId)) {
            throw new MemberDepartmentAlreadyExistsException();
        }

        OrganizationMember member = orgMemberService.getMemberById(memberId, organization.getId());
        Department department = departmentRepository
                .findByIdAndOrganizationId(departmentId, organization.getId())
                .orElseThrow(DepartmentNotFoundException::new);

        memberDepartmentRepository.save(MemberDepartment.create(member, department));
    }

    /**
     * 조직원의 부서 지정 해제
     */
    @Transactional
    public void removeDepartment(Long memberId, Long departmentId) {
        organizationService.getOrgAfterValidateOwnerOrBoss();

        MemberDepartment memberDepartment = memberDepartmentRepository.findByDepartmentIdAndOrganizationMemberId(departmentId, memberId)
                        .orElseThrow(MemberDepartmentNotInException::new);

        memberDepartmentRepository.delete(memberDepartment);
    }

}
