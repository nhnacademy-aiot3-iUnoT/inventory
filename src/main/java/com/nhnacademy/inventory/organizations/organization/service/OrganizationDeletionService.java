package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationDeletionService {
    private final MemberDepartmentRepository memberDepartmentRepository;
    private final StorageDepartmentRepository storageDepartmentRepository;
    private final OrganizationMemberRepository orgMemberRepository;
    private final DepartmentRepository departmentRepository;
    private final StorageRepository storageRepository;
    private final ZoneRepository zoneRepository;
    private final InvitationRepository invitationRepository;

    public void softDelete(Organization organization) {

        Long id = organization.getId();
        // 1. 연결 테이블 삭제
        memberDepartmentRepository.deleteByOrganizationId(id);
        storageDepartmentRepository.deleteByOrganizationId(id);

        // 2. 부모 삭제
        orgMemberRepository.deleteByOrganizationId(id);
        departmentRepository.deleteByOrganizationId(id);

        // 3. 상태 변경
        storageRepository.closeByOrganizationId(id);
        zoneRepository.closeByOrganizationId(id);

        invitationRepository.cancelByOrganizationId(id);

        organization.suspended();
    }
}
