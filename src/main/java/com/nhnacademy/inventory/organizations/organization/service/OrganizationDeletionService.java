package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.global.cient.AccountClient;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    private final AccountClient accountClient;

    public void softDelete(Organization organization) {
        Long id = organization.getId();
        List<UUID> accountUuids = orgMemberRepository.findAccountUuidsByOrganizationId(id);

        // 1. 연결 테이블 삭제
        memberDepartmentRepository.deleteByOrganizationId(id);
        storageDepartmentRepository.deleteByOrganizationId(id);

        // 2. 부모 삭제
        orgMemberRepository.deleteByOrganizationId(id);
        departmentRepository.deleteByOrganizationId(id);

        // 3. 상태 변경
        storageRepository.closeByOrganizationId(id);
        zoneRepository.closeByOrganizationId(id);

        // 4. 초대 삭제
        invitationRepository.deleteByOrganizationId(id);

        organization.suspended();

        if (!accountUuids.isEmpty()) {
            accountClient.deleteAccounts(accountUuids);
        }
    }

    public void deletePendingRelations(Long organizationId) {
        orgMemberRepository.deleteByOrganizationId(organizationId);

        invitationRepository.deleteByOrganizationId(organizationId);
    }

    public void deleteMember(OrganizationMember member) {
        AccountResponse response = accountClient.deleteAccount(member.getAccountUuid());
        String email = response.email();

        invitationRepository.deleteByOrganizationIdAndEmail(member.getOrganization().getId(), email);
        memberDepartmentRepository.deleteByOrganizationMemberId(member.getId());
        orgMemberRepository.delete(member);
    }
}
