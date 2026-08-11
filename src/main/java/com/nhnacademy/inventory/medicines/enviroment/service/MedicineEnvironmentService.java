package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.global.util.UserContext;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.exception.EnvironmentStandardNotFoundException;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;


import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentTypeRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitNotFoundException;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;

import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;

import java.util.*;

import com.nhnacademy.inventory.organizations.organization.exception.OrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineEnvironmentService {


    private final MedicineEnvironmentStandardRepository standardRepository;
    private final MedicineEnvironmentTypeRepository typeRepository;
    private final OrganizationMemberRepository memberRepository;
    private final MedicinePackageUnitRepository packageUnitRepository;



    // 조회용 - 기존데이터 가져오기
    @Transactional(readOnly = true)
    public List<MedicineEnvironmentType> getType(Long packageUnitId){

        OrganizationMember organizationMember = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(UserOrgNotFoundException::new);

        Organization organization = organizationMember.getOrganization();

        MedicineEnvironmentStandard standard = standardRepository.findByOrganizationIdAndPackageUnitIdForUpdate(organization.getId(),packageUnitId)
                .orElse(null);

        if(standard == null ){
            return List.of();
        }

        return typeRepository.findAllByMedicineEnvironmentStandardId(standard.getId());

    }



    // 환경유형 저장
    @Transactional
    public void createTypes(MedicineEnvironmentRequest request){


        OrganizationMember organizationMember = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(UserOrgNotFoundException::new);

        Organization organization = organizationMember.getOrganization();

        MedicineEnvironmentStandard standard = standardRepository.findByOrganizationIdAndPackageUnitId(organization.getId(),request.medicinePackageUnitId())
                .orElse(null);


        //기준이 없다..! 그럼 저장
        if(standard == null){

            MedicinePackageUnit packageUnit = packageUnitRepository.findById(request.medicinePackageUnitId())
                    .orElseThrow(PackUnitNotFoundException::new);

            MedicineEnvironmentStandard newStandard = MedicineEnvironmentStandard.create(
                    packageUnit,
                    organization,
                    UserContext.getUserUuid()
            );

            List<MedicineEnvironmentType> types = makeTypes(newStandard,request);

            typeRepository.saveAll(types);

        }


    }

    // 환경 유형 수정
    @Transactional
    public void updateTypes(MedicineEnvironmentRequest request){


        OrganizationMember organizationMember = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(UserOrgNotFoundException::new);

        Organization organization = organizationMember.getOrganization();

        MedicineEnvironmentStandard standard = standardRepository.findByOrganizationIdAndPackageUnitId(organization.getId(),request.medicinePackageUnitId())
                .orElseThrow(EnvironmentStandardNotFoundException::new);

        typeRepository.deleteAllByMedicineEnvironmentStandardId(standard.getId());
        List<MedicineEnvironmentType> types = makeTypes(standard,request);

        typeRepository.saveAll(types);
        standard.updateIdAndAt(UserContext.getUserUuid());


    }



    // 환경 유형 삭제
    @Transactional
    public void deleteTypes(Long standardId){


        standardRepository.deleteById(standardId);
        typeRepository.deleteAllByMedicineEnvironmentStandardId(standardId);


    }



    // 환경 유형 생성
    private List<MedicineEnvironmentType> makeTypes(MedicineEnvironmentStandard standard,MedicineEnvironmentRequest request){

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
