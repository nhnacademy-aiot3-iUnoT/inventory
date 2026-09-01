package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.global.client.AccountClient;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganizationDeletionServiceTest {
    @Mock
    private MemberDepartmentRepository memberDepartmentRepository;

    @Mock
    private StorageDepartmentRepository storageDepartmentRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private OrganizationDeletionService organizationDeletionService;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "12345");
        ReflectionTestUtils.setField(organization, "id", 1L);
    }

    @Test
    @DisplayName("조직 Soft Delete 성공")
    void softDelete_success() {
        Long organizationId = organization.getId();
        List<UUID> accountUuids = List.of(UUID.randomUUID(), UUID.randomUUID());

        given(organizationMemberRepository.findAccountUuidsByOrganizationId(organizationId))
                .willReturn(accountUuids);

        organizationDeletionService.softDelete(organization);

        verify(organizationMemberRepository).findAccountUuidsByOrganizationId(organizationId);
        verify(memberDepartmentRepository).deleteByOrganizationId(organizationId);
        verify(storageDepartmentRepository).deleteByOrganizationId(organizationId);
        verify(organizationMemberRepository).deleteByOrganizationId(organizationId);
        verify(departmentRepository).deleteByOrganizationId(organizationId);
        verify(storageRepository).closeByOrganizationId(organizationId);
        verify(zoneRepository).closeByOrganizationId(organizationId);
        verify(invitationRepository).deleteByOrganizationId(organizationId);
        verify(accountClient).deleteAccounts(accountUuids);

        assertEquals(OrganizationStatus.SUSPENDED, organization.getStatus());
    }

    @Test
    @DisplayName("조직 Soft Delete 성공 - 조직원 없음")
    void softDelete_empty_members_success() {
        Long organizationId = organization.getId();

        given(organizationMemberRepository.findAccountUuidsByOrganizationId(organizationId))
                .willReturn(List.of());

        organizationDeletionService.softDelete(organization);

        verify(organizationMemberRepository).findAccountUuidsByOrganizationId(organizationId);
        verify(memberDepartmentRepository).deleteByOrganizationId(organizationId);
        verify(storageDepartmentRepository).deleteByOrganizationId(organizationId);
        verify(organizationMemberRepository).deleteByOrganizationId(organizationId);
        verify(departmentRepository).deleteByOrganizationId(organizationId);
        verify(storageRepository).closeByOrganizationId(organizationId);
        verify(zoneRepository).closeByOrganizationId(organizationId);
        verify(invitationRepository).deleteByOrganizationId(organizationId);

        verify(accountClient, never()).deleteAccounts(any());

        assertEquals(OrganizationStatus.SUSPENDED, organization.getStatus());
    }

    @Test
    @DisplayName("PENDING 조직 관계 삭제 성공")
    void deletePendingRelations_success() {
        Long organizationId = 1L;
        UUID accountUuid = UUID.randomUUID();
        given(organizationMemberRepository.findAccountUuidByOrganizationId(organizationId))
                .willReturn(java.util.Optional.of(accountUuid));

        organizationDeletionService.deletePendingRelations(organizationId);

        verify(organizationMemberRepository).findAccountUuidByOrganizationId(organizationId);
        verify(organizationMemberRepository).deleteByOrganizationId(organizationId);
        verify(invitationRepository).deleteByOrganizationId(organizationId);
        verify(accountClient).deleteAccount(accountUuid);
    }

    @Test
    @DisplayName("조직원 삭제 성공")
    void deleteMember_success() {
        OrganizationMember member = TestFixtures.createOrganizationMember(organization);
        ReflectionTestUtils.setField(member, "id", 1L);

        UUID accountUuid = member.getAccountUuid();
        AccountResponse accountResponse = new AccountResponse(accountUuid, "테스트 사용자", "test@email.com");

        given(accountClient.deleteAccount(accountUuid)).willReturn(accountResponse);

        organizationDeletionService.deleteMember(member);

        verify(accountClient).deleteAccount(accountUuid);
        verify(invitationRepository).deleteByOrganizationIdAndEmail(organization.getId(), "test@email.com");
        verify(memberDepartmentRepository).deleteByOrganizationMemberId(member.getId());
        verify(organizationMemberRepository).delete(member);
    }
}
