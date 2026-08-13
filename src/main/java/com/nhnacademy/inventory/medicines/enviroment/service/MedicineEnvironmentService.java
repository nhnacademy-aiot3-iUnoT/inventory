package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.global.util.UserContext;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.exception.EnvironmentStandardNotFoundException;
import com.nhnacademy.inventory.medicines.enviroment.operation.EnvironmentStandardCreator;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;


import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentTypeRepository;
import com.nhnacademy.inventory.medicines.medicine.exception.MedicineNotFoundException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;

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


    // 환경유형 저장 및 수정
    @Transactional
    public void createTypes(Long medicinePackageUnitId ,MedicineEnvironmentRequest request){

        UUID accountId = UserContext.getUserUuid();
        OrganizationMember organizationMember = memberRepository.findByAccountUuid(accountId)
                .orElseThrow(UserOrgNotFoundException::new);
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
    public void updateTypes(Long medicinePackageUnitId,MedicineEnvironmentRequest request){


        UUID accountId = UserContext.getUserUuid();
        OrganizationMember organizationMember = memberRepository.findByAccountUuid(accountId)
                .orElseThrow(UserOrgNotFoundException::new);
        Organization organization = organizationMember.getOrganization();
        MedicineEnvironmentStandard standard = standardRepository.findByOrganizationIdAndPackageUnitIdForUpdate(organization.getId(),medicinePackageUnitId)
                .orElse(null);

        if(standard == null){
            // 기준 추가
            // 로직
            environmentStandardCreator.createStandard(medicinePackageUnitId,organization,accountId,request);
            return;
        }

        //기존이 있다.
        typeRepository.deleteAllByMedicineEnvironmentStandardId(standard.getId());
        List<MedicineEnvironmentType> types = environmentStandardCreator.makeTypes(standard,request);

        if(types.isEmpty()){
            standardRepository.deleteById(standard.getId());
            return;
        }

        typeRepository.saveAll(types);
        standard.updateIdAndAt(UserContext.getUserUuid());

    }


    // 환경 유형 삭제
    @Transactional
    public void deleteTypes(Long standardId){

        MedicineEnvironmentStandard standard = standardRepository.findById(standardId)
                        .orElseThrow(EnvironmentStandardNotFoundException::new);

        typeRepository.deleteAllByMedicineEnvironmentStandardId(standard.getId());
        standardRepository.deleteById(standard.getId());

    }




}
