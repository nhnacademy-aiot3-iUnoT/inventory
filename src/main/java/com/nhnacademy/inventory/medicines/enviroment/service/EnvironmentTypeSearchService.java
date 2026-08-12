package com.nhnacademy.inventory.medicines.enviroment.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentTypeResponse;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentStandardRepository;
import com.nhnacademy.inventory.medicines.enviroment.repository.MedicineEnvironmentTypeRepository;

import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentTypeSearchService {


    private final MedicineEnvironmentStandardRepository standardRepository;
    private final MedicineEnvironmentTypeRepository typeRepository;
    private final OrganizationMemberRepository memberRepository;


    // 조회용 - 기존데이터 가져오기
    @Transactional(readOnly = true)
    public List<MedicineEnvironmentTypeResponse> getTypes(Long packageUnitId){

        OrganizationMember organizationMember = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(UserOrgNotFoundException::new);
        Organization organization = organizationMember.getOrganization();
        MedicineEnvironmentStandard standard = standardRepository.findByOrganizationIdAndMedicinePackageUnitId(organization.getId(),packageUnitId)
                .orElse(null);

        // 목록 없다면 환경기준 없다고 판단.
        if(standard == null ){
            return List.of();
        }

        // 있다면 그전 의약품 정보 가져올 수 있게
        List<MedicineEnvironmentType> types = typeRepository.findAllByMedicineEnvironmentStandardId(standard.getId());
        List<MedicineEnvironmentTypeResponse> responses = types.stream()
                .map(MedicineEnvironmentTypeResponse::from)
                .toList();

        log.info("MedicineEnvironmentType response : {}", responses);

        return responses;


    }




}
