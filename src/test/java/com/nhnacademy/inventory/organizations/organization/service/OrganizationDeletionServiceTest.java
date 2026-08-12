package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
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

import static org.junit.jupiter.api.Assertions.*;
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

        organizationDeletionService.softDelete(organization);

        verify(memberDepartmentRepository).deleteByOrganizationId(organizationId);

        verify(storageDepartmentRepository).deleteByOrganizationId(organizationId);

        verify(organizationMemberRepository).deleteByOrganizationId(organizationId);

        verify(departmentRepository).deleteByOrganizationId(organizationId);

        verify(storageRepository).closeByOrganizationId(organizationId);

        verify(zoneRepository).closeByOrganizationId(organizationId);

        verify(invitationRepository).deleteByOrganizationId(organizationId);

        assertEquals(OrganizationStatus.SUSPENDED, organization.getStatus());
    }

    @Test
    @DisplayName("PENDING 조직 관계 삭제 성공")
    void deletePendingRelations_success() {
        Long organizationId = 1L;

        organizationDeletionService.deletePendingRelations(
                organizationId
        );

        verify(organizationMemberRepository).deleteByOrganizationId(organizationId);

        verify(invitationRepository).deleteByOrganizationId(organizationId);
    }

}
