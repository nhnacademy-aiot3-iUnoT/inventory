package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.operation.EnvironmentStandardCreator;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentTypeRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
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
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
        given(memberRepository.findByAccountUuid(accountId)).willReturn(Optional.of(organizationMember));
        given(organizationMember.getOrganizationRole()).willReturn(OrganizationRole.ORG_BOSS);


        Organization organization = mock(Organization.class);

        given(organizationMember.getOrganization()).willReturn(organization);
        given(organization.getId()).willReturn(1L);


        MedicineEnvironmentStandard standard = mock(MedicineEnvironmentStandard.class);

        given(standardRepository.findByOrganizationIdAndPackageUnitIdForUpdate(1L,1L))
                        .willReturn(Optional.of(standard));


        MedicineEnvironmentType tepType = MedicineEnvironmentType.create(
                standard,
                EnvironmentType.TEMPERATURE,
                BigDecimal.valueOf(10L),
                BigDecimal.valueOf(30L)
                );


        MedicineEnvironmentType humType = MedicineEnvironmentType.create(
                standard,
                EnvironmentType.HUMIDITY,
                BigDecimal.valueOf(10L),
                BigDecimal.valueOf(50L)

        );

        MedicineEnvironmentType illType = MedicineEnvironmentType.create(
                standard,
                EnvironmentType.ILLUMINANCE,
                BigDecimal.valueOf(100L),
                BigDecimal.valueOf(200L)
        );

        given(typeRepository.findByMedicineEnvironmentStandardAndEnvironmentType(standard,EnvironmentType.TEMPERATURE))
                .willReturn(Optional.of(tepType));

        given(typeRepository.findByMedicineEnvironmentStandardAndEnvironmentType(standard,EnvironmentType.HUMIDITY))
                .willReturn(Optional.of(humType));

        given(typeRepository.findByMedicineEnvironmentStandardAndEnvironmentType(standard,EnvironmentType.ILLUMINANCE))
                .willReturn(Optional.of(illType));

        MedicineEnvironmentRequest request = new MedicineEnvironmentRequest(
                BigDecimal.valueOf(20L),
                BigDecimal.valueOf(60L),
                BigDecimal.valueOf(30L),
                BigDecimal.valueOf(80L),
                BigDecimal.valueOf(100L),
                BigDecimal.valueOf(200L)


        );


        medicineEnvironmentService.updateTypes(1L,request);

        assertAll(

                () -> assertEquals(BigDecimal.valueOf(20L),tepType.getMin()),
                () -> assertEquals(BigDecimal.valueOf(60L),tepType.getMax()),
                () -> assertEquals(BigDecimal.valueOf(30L),humType.getMin()),
                () -> assertEquals(BigDecimal.valueOf(80L),humType.getMax()),
                () -> assertEquals(BigDecimal.valueOf(100L),illType.getMin()),
                () -> assertEquals(BigDecimal.valueOf(200L),illType.getMax())

        );







    }

    @Test
    @DisplayName("환경 유형 삭제")
    void deleteTypes() {


        OrganizationMember organizationMember = mock(OrganizationMember.class);

        given(memberRepository.findByAccountUuid(accountId))
                .willReturn(Optional.of(organizationMember));

        given(organizationMember.getOrganizationRole()).willReturn(OrganizationRole.ORG_BOSS);


        MedicineEnvironmentStandard standard = mock(MedicineEnvironmentStandard.class);

        given(standardRepository.findByMedicinePackageUnitId(anyLong())).willReturn(Optional.of(standard));

        given(standard.getId()).willReturn(1L);

        medicineEnvironmentService.deleteTypes(1L);

        verify(typeRepository).deleteAllByMedicineEnvironmentStandardId(anyLong());
        verify(standardRepository).deleteById(anyLong());



    }
}