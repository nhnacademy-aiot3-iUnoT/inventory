package com.nhnacademy.inventory.medicines.enviroment.operation;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentTypeRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitNotFoundException;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnvironmentStandardCreator {

    private final MedicineEnvironmentStandardRepository standardRepository;
    private final MedicineEnvironmentTypeRepository typeRepository;
    private final MedicinePackageUnitRepository packageUnitRepository;


    // 새 기준 추가
    public void createStandard(Long packageUnitId,Organization organization, UUID accountId, MedicineEnvironmentRequest request){


        MedicinePackageUnit packageUnit = packageUnitRepository.findById(packageUnitId)
                .orElseThrow(PackUnitNotFoundException::new);

        MedicineEnvironmentStandard newStandard = MedicineEnvironmentStandard.create(
                packageUnit,
                organization,
                accountId
        );

        List<MedicineEnvironmentType> types = makeTypes(newStandard,request);

        if(types.isEmpty()){
            return;
        }

        standardRepository.save(newStandard);
        typeRepository.saveAll(types);


    }


    // 환경 유형 생성
    public List<MedicineEnvironmentType> makeTypes(MedicineEnvironmentStandard standard,MedicineEnvironmentRequest request){

        List<MedicineEnvironmentType> types = new ArrayList<>();


        if(request.minTemperature() != null && request.maxTemperature() != null){

            MedicineEnvironmentType type = MedicineEnvironmentType.create(
                    standard,
                    EnvironmentType.TEMPERATURE,
                    request.minTemperature(),
                    request.maxTemperature()
            );

            types.add(type);

        }

        if(request.minHumidity() != null && request.maxHumidity() != null){

            MedicineEnvironmentType type = MedicineEnvironmentType.create(
                    standard,
                    EnvironmentType.HUMIDITY,
                    request.minHumidity(),
                    request.maxHumidity()
            );

            types.add(type);
        }


        if(request.minIlluminance() != null && request.maxIlluminance() != null){

            MedicineEnvironmentType type = MedicineEnvironmentType.create(
                    standard,
                    EnvironmentType.ILLUMINANCE,
                    request.minIlluminance(),
                    request.maxIlluminance()
            );

            types.add(type);

        }



        return types;


    }





}
