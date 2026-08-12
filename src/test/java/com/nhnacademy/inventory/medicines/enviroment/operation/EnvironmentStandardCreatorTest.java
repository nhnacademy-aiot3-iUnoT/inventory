package com.nhnacademy.inventory.medicines.enviroment.operation;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentTypeRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnvironmentStandardCreatorTest {


    @Mock
    MedicineEnvironmentStandardRepository standardRepository;
    @Mock
    MedicineEnvironmentTypeRepository typeRepository;
    @Mock
    MedicinePackageUnitRepository packageUnitRepository;
    @InjectMocks
    EnvironmentStandardCreator environmentStandardCreator;

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
    @DisplayName("새 기준 추가 검증")
    void createStandard() {


        MedicinePackageUnit packageUnit = mock(MedicinePackageUnit.class);
        Organization organization = mock(Organization.class);


        MedicineEnvironmentRequest request = new MedicineEnvironmentRequest(
                new BigDecimal("1"),
                new BigDecimal("30"),
                null,
                null,
                null,
                null
        );



        given(packageUnitRepository.findById(1L)).willReturn(Optional.of(packageUnit));

        //내가 만든 standard랑 newStandard랑 달라서 오류
        environmentStandardCreator.createStandard(1L,organization,accountId,request);

        verify(standardRepository).save(any(MedicineEnvironmentStandard.class));
        verify(typeRepository).saveAll(anyList());


    }

    @Test
    @DisplayName("환경 타입 생성")
    void makeTypes() {


        MedicineEnvironmentStandard standard = mock(MedicineEnvironmentStandard.class);
        MedicineEnvironmentRequest request = new MedicineEnvironmentRequest(
                new BigDecimal("1"),
                new BigDecimal("30"),
                new BigDecimal("10"),
                new BigDecimal("40"),
                new BigDecimal("10"),
                new BigDecimal("20")
        );

        List<MedicineEnvironmentType> types = environmentStandardCreator.makeTypes(standard,request);


        assertAll(



                () -> assertEquals(3,types.size()),
                () -> assertEquals(EnvironmentType.TEMPERATURE,types.getFirst().getEnvironmentType()),
                () -> assertEquals(request.minTemperature(),types.getFirst().getMin()),
                () -> assertEquals(request.maxTemperature(),types.getFirst().getMax()),

                () -> assertEquals(EnvironmentType.HUMIDITY,types.get(1).getEnvironmentType()),
                () -> assertEquals(request.minHumidity(),types.get(1).getMin()),
                () -> assertEquals(request.maxHumidity(),types.get(1).getMax()),

                () -> assertEquals(EnvironmentType.ILLUMINANCE,types.get(2).getEnvironmentType()),
                () -> assertEquals(request.minIlluminance(),types.get(2).getMin()),
                () -> assertEquals(request.maxIlluminance(),types.get(2).getMax())



        );







    }
}