package com.nhnacademy.inventory.organizations.department.service;

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
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
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

    @Transactional
    public void assignDepartment(Long memberId, Long departmentId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();

        if (memberDepartmentRepository.existsByOrganizationMemberIdAndDepartmentId(memberId, departmentId)) {
            throw new MemberDepartmentAlreadyExistsException();
        }

        OrganizationMember member = orgMemberService.getMemberById(memberId, organization.getId());

        Department department = departmentRepository.findByIdAndOrganizationId(departmentId, organization.getId())
                        .orElseThrow(DepartmentNotFoundException::new);

        memberDepartmentRepository.save(MemberDepartment.create(member, department));
    }

    @Transactional
    public void removeDepartment(Long memberId, Long departmentId) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();

        OrganizationMember member = orgMemberService.getMemberById(memberId, organization.getId());

        Department department = departmentRepository.findByIdAndOrganizationId(departmentId, organization.getId())
                .orElseThrow(DepartmentNotFoundException::new);

        MemberDepartment memberDepartment = memberDepartmentRepository.findByDepartmentIdAndOrganizationMemberId(department.getId(), member.getId())
                .orElseThrow(MemberDepartmentNotInException::new);

        memberDepartmentRepository.delete(memberDepartment);
    }
}
