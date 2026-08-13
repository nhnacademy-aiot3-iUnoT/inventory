package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.operation.EnvironmentStandardCreator;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentTypeRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MedicineEnvironmentServiceTest {


    @Mock
    MedicineEnvironmentStandardRepository standardRepository;
    @Mock
    MedicineEnvironmentTypeRepository typeRepository;
    @Mock
    OrganizationMemberRepository memberRepository;
    @Mock
    EnvironmentStandardCreator environmentStandardCreator;
    @InjectMocks
    MedicineEnvironmentService medicineEnvironmentService;

    UUID accountId;


    @BeforeEach
    void setUp(){

        accountId = UUID.randomUUID();
        UserContext.setUserUuid(accountId);

    }

    @AfterEach
    void afterSetUp(){

        UserContext.setUserUuid(accountId);
    }

    @Test
    @DisplayName("환경 유형 저장")
    void createTypes() {


        Organization organization = mock(Organization.class);
        OrganizationMember organizationMember = mock(OrganizationMember.class);
        MedicineEnvironmentRequest request = mock(MedicineEnvironmentRequest.class);


        given(memberRepository.findByAccountUuid(accountId)).willReturn(Optional.of(organizationMember));
        given(organizationMember.getOrganization()).willReturn(organization);
        given(standardRepository.findByOrganizationIdAndPackageUnitIdForUpdate(organization.getId(),1L))
                .willReturn(Optional.empty());

        medicineEnvironmentService.createTypes(1L,request);


        verify(environmentStandardCreator).createStandard(1L,organization,accountId,request);


    }


    @Test
    @DisplayName("환경 유형 수정")
    void updateTypes() {


        OrganizationMember organizationMember = mock(OrganizationMember.class);
        Organization organization = mock(Organization.class);
        MedicineEnvironmentStandard standard = mock(MedicineEnvironmentStandard.class);
        MedicineEnvironmentRequest request = mock(MedicineEnvironmentRequest.class);

        // 기준이 없는 경우

        given(memberRepository.findByAccountUuid(accountId)).willReturn(Optional.of(organizationMember));
        given(organizationMember.getOrganization()).willReturn(organization);
        given(organization.getId()).willReturn(1L);
        given(standardRepository.findByOrganizationIdAndPackageUnitIdForUpdate(organization.getId(),1L))
                .willReturn(Optional.empty());

        medicineEnvironmentService.updateTypes(1L,request);

        verify(environmentStandardCreator).createStandard(1L,organization,accountId,request);


        // 기준이 있는 경우


        MedicineEnvironmentType type = mock(MedicineEnvironmentType.class);
        List<MedicineEnvironmentType> types = List.of(type);

        given(standardRepository.findByOrganizationIdAndPackageUnitIdForUpdate(organization.getId(),1L))
                .willReturn(Optional.of(standard));
        given(standard.getId()).willReturn(1L);
        given(environmentStandardCreator.makeTypes(standard,request))
                .willReturn(types);


        medicineEnvironmentService.updateTypes(1L,request);


        verify(typeRepository).deleteAllByMedicineEnvironmentStandardId(standard.getId());
        verify(typeRepository).saveAll(types);
        verify(standard).updateIdAndAt(accountId);




    }

    @Test
    @DisplayName("환경 유형 삭제")
    void deleteTypes() {

        MedicineEnvironmentStandard standard = mock(MedicineEnvironmentStandard.class);
        given(standardRepository.findById(1L)).willReturn(Optional.of(standard));
        given(standard.getId()).willReturn(1L);

        medicineEnvironmentService.deleteTypes(1L);

        verify(typeRepository).deleteAllByMedicineEnvironmentStandardId(standard.getId());
        verify(standardRepository).deleteById(standard.getId());




    }
}