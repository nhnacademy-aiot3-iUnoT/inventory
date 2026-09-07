package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.exception.EnvironmentStandardNotFoundException;
import com.nhnacademy.inventory.medicines.enviroment.operation.EnvironmentStandardCreator;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;

import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentTypeRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;

import java.math.BigDecimal;
import java.util.*;

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
    private final EnvironmentStandardCreator environmentStandardCreator;


    // 입고용 환경기준, 유형 저장
    @Transactional
    public void createTypes(Long medicinePackageUnitId ,MedicineEnvironmentRequest request){

        UUID accountId = UserContext.getUserUuid();
        OrganizationMember organizationMember = memberRepository.findByAccountUuid(accountId)
                .orElseThrow(UserOrgNotFoundException::new);


        if(organizationMember.getOrganizationRole() == OrganizationRole.ORG_MEMBER){
            throw new ForbiddenException();
        }

        Organization organization = organizationMember.getOrganization();
        MedicineEnvironmentStandard standard = standardRepository.findByOrganizationIdAndPackageUnitIdForUpdate(organization.getId(),medicinePackageUnitId)
                .orElse(null);


        // 기준이 있다 그럼 저장 x 사용자는 조회된 기준으로 사용
        // -> 환경 기준 수정 부분에서 수정가능

        if(standard != null){
            return;
        }
        //기준이 없다..! 그럼 저장
        environmentStandardCreator.createStandard(medicinePackageUnitId,organization,accountId,request);

    }


    // request 0 -> 수정  request 모두 null -> 타입, 기준 모두 삭제
    @Transactional
    public void updateTypes(Long medicinePackageUnitId, MedicineEnvironmentRequest request){

        UUID accountId = UserContext.getUserUuid();
        OrganizationMember organizationMember = memberRepository.findByAccountUuid(accountId)
                .orElseThrow(UserOrgNotFoundException::new);

        if(organizationMember.getOrganizationRole() == OrganizationRole.ORG_MEMBER){
            throw new ForbiddenException();
        }


        Organization organization = organizationMember.getOrganization();
        MedicineEnvironmentStandard standard = standardRepository.findByOrganizationIdAndPackageUnitIdForUpdate(organization.getId(),medicinePackageUnitId)
                .orElseThrow(EnvironmentStandardNotFoundException::new);


        MedicineEnvironmentType tepType = typeRepository.findByMedicineEnvironmentStandardAndEnvironmentType(standard,EnvironmentType.TEMPERATURE)
                        .orElse(null);
        MedicineEnvironmentType humType = typeRepository.findByMedicineEnvironmentStandardAndEnvironmentType(standard,EnvironmentType.HUMIDITY)
            .orElse(null);
        MedicineEnvironmentType illType = typeRepository.findByMedicineEnvironmentStandardAndEnvironmentType(standard,EnvironmentType.ILLUMINANCE)
            .orElse(null);



        updateEnvironment(standard,tepType,EnvironmentType.TEMPERATURE,request.minTemperature(),request.maxTemperature());
        updateEnvironment(standard,humType, EnvironmentType.HUMIDITY,request.minHumidity(),request.maxHumidity());
        updateEnvironment(standard,illType,EnvironmentType.ILLUMINANCE,request.minIlluminance(),request.maxIlluminance());

        standard.updateIdAndAt(accountId);

    }

    // 환경 유형 삭제
    @Transactional
    public void deleteTypes(Long packUnitId){

        UUID accountId = UserContext.getUserUuid();
        OrganizationMember organizationMember = memberRepository.findByAccountUuid(accountId)
                .orElseThrow(UserOrgNotFoundException::new);

        if(organizationMember.getOrganizationRole() == OrganizationRole.ORG_MEMBER){
            throw new ForbiddenException();
        }


        MedicineEnvironmentStandard standard = standardRepository.findByMedicinePackageUnitId(packUnitId)
                        .orElseThrow(EnvironmentStandardNotFoundException::new);

        typeRepository.deleteAllByMedicineEnvironmentStandardId(standard.getId());
        standardRepository.deleteById(standard.getId());

    }
    

    private void updateEnvironment(MedicineEnvironmentStandard standard, MedicineEnvironmentType type, EnvironmentType status, BigDecimal min, BigDecimal max){

        if(type == null){

            if(min != null && max!= null){

                MedicineEnvironmentType createType = MedicineEnvironmentType.create(standard,status,min,max);
                typeRepository.save(createType);

            }

        }
        else{

            if(min == null && max == null){
                typeRepository.deleteByMedicineEnvironmentStandardAndEnvironmentType(
                        standard,status
                );

            }

            else if(min != null && max != null){
                type.update(min,max);

            }

        }

    }



}
