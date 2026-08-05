package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.global.util.UserContext;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;

import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentStandardRequest;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentStandardResponse;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentTypeRequest;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentTypeResponse;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;



import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitNotFoundException;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.exception.OrganizationMemberNotFoundException;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineEnvironmentService {

    private final MedicineEnvironmentStandardRepository standardRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final MedicinePackageUnitRepository packageUnitRepository;


    // 의약품 환경 기준 조회
    @Transactional(readOnly = true)
    public MedicineEnvironmentStandardResponse getStandard(Long medicinePackUnitId){

        UUID accountId = UserContext.getUserUuid();
        OrganizationMember organizationMember = organizationMemberRepository.findByAccountUuid(accountId)
                        .orElseThrow(OrganizationMemberNotFoundException::new);

        log.info("OrganizationMember: {}",organizationMember);

        Long organizationId = organizationMember.getOrganization().getId();
        MedicineEnvironmentStandard standard = standardRepository.findWithEnvironmentTypes(organizationId,medicinePackUnitId)
                .orElse(null);

        if(standard == null){

            return new MedicineEnvironmentStandardResponse(null,medicinePackUnitId, List.of());

        }

        List<MedicineEnvironmentTypeResponse> typeResponse = standard.getEnvironmentTypes().stream()
                .map(type -> new MedicineEnvironmentTypeResponse(
                        type.getEnvironmentType(),
                        type.getMin(),
                        type.getMax()

                )).toList();

        log.info("typeResponse: {}",typeResponse);


        return new MedicineEnvironmentStandardResponse(
                standard.getId(),
                standard.getMedicinePackageUnit().getId(),
                typeResponse
                );

    }

    // 의약품 환경기준 초기 설정
    @Transactional
    public void saveOrUpdate(Long medicinePackageUnitId, MedicineEnvironmentStandardRequest request){

        UUID accountId = UserContext.getUserUuid();
        OrganizationMember organizationMember = organizationMemberRepository.findByAccountUuid(accountId)
                .orElseThrow(OrganizationMemberNotFoundException::new);

        Organization organization = organizationMember.getOrganization();
        MedicineEnvironmentStandard standard = standardRepository.findWithEnvironmentTypes(organization.getId(),medicinePackageUnitId)
                .orElse(null);


        // 이전 의약품 환경기준 o , 환경 기준 설정할때
        if(standard != null){

            updateExistingStandard(standard,request.environmentTypes(),accountId);
            return;

        }

        // 이전 의약품 환경기준 x , 환경 기준 설정하지 않을 때 -> 저장하지 않음
        if(request.environmentTypes().isEmpty()){
            return;
        }


        MedicinePackageUnit medicinePackageUnit = packageUnitRepository.findById(medicinePackageUnitId)
                .orElseThrow(PackUnitNotFoundException::new);

        // 의약품 환경 기준 새로 설정
        MedicineEnvironmentStandard newStandard = MedicineEnvironmentStandard.create(
                medicinePackageUnit,
                organization,
                accountId
        );

        for(MedicineEnvironmentTypeRequest typeRequest : request.environmentTypes()){
            newStandard.addEnvironmentType(typeRequest.environmentType(),typeRequest.min(),typeRequest.max());

        }

        standardRepository.save(newStandard);

    }


    private void updateExistingStandard(MedicineEnvironmentStandard standard,List<MedicineEnvironmentTypeRequest> typeRequests,UUID updateAccountId){

        if(typeRequests.isEmpty()){
            return;
        }

        standard.updateEnvironmentTypes(typeRequests);
        standard.updateAccount(updateAccountId);

    }





}
