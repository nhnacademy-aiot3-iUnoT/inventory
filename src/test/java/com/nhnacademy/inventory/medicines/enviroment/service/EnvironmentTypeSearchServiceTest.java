package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvironmentTypeSearchServiceTest {

    @Mock
    MedicineEnvironmentStandardRepository standardRepository;
    @Mock
    MedicineEnvironmentTypeRepository typeRepository;
    @Mock
    OrganizationMemberRepository memberRepository;

    @InjectMocks
    EnvironmentTypeSearchService environmentTypeSearchService;

    UUID accountId;

    @BeforeEach
    void setUp(){

        accountId = UUID.randomUUID();
        UserContext.setUserUuid(accountId);
    }


    @AfterEach
    void afterSetUp(){
        UserContext.clear();

    }

    @Test
    @DisplayName("환경유형 기존데이터 조회")
    void getTypes() {


        OrganizationMember organizationMember = mock(OrganizationMember.class);
        MedicineEnvironmentStandard standard = mock(MedicineEnvironmentStandard.class);
        MedicineEnvironmentType type = mock(MedicineEnvironmentType.class);
        Organization organization = mock(Organization.class);




        given(memberRepository.findByAccountUuid(accountId))
                .willReturn(Optional.of(organizationMember));
        given(organizationMember.getOrganization()).willReturn(organization);
        given(organization.getId()).willReturn(1L);
        given(standardRepository.findByOrganizationIdAndMedicinePackageUnitId(organization.getId(), 1L))
                .willReturn(Optional.of(standard));
        given(standard.getId()).willReturn(1L);
        given(typeRepository.findAllByMedicineEnvironmentStandardId(standard.getId()))
                .willReturn(List.of(type));


        environmentTypeSearchService.getTypes(1L);


        verify(memberRepository).findByAccountUuid(accountId);
        verify(standardRepository).findByOrganizationIdAndMedicinePackageUnitId(1L,1L);
        verify(typeRepository).findAllByMedicineEnvironmentStandardId(1L);



    }

    @Test
    @DisplayName("환경 기준이 없을 경우")
    void standardNullTest(){


        OrganizationMember organizationMember = mock(OrganizationMember.class);
        Organization organization = mock(Organization.class);



        given(memberRepository.findByAccountUuid(accountId)).willReturn(Optional.of(organizationMember));
        given(organizationMember.getOrganization()).willReturn(organization);
        given(organization.getId()).willReturn(1L);
        given(standardRepository.findByOrganizationIdAndMedicinePackageUnitId(
                organization.getId(),1L))
                .willReturn(Optional.empty());

        assertEquals(List.of(),environmentTypeSearchService.getTypes(1L));

        verify(memberRepository).findByAccountUuid(accountId);
        verify(standardRepository).findByOrganizationIdAndMedicinePackageUnitId(1L,1L);
        verify(typeRepository,never()).findAllByMedicineEnvironmentStandardId(1L);


    }




}