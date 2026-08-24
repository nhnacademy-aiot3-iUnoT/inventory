package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoriesSearchService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final MemberDepartmentRepository memberDepartmentRepository;
    private final StorageRepository storageRepository;



    // 해당 부서 - 저장소에 해당하는 전체 인벤토리 조회
    @Transactional(readOnly = true)
    public Page<InventoriesResponse> getInventories(String search, Long storageId, Pageable pageable){



        UUID accountId = UserContext.getUserUuid();
        OrganizationMember member=  organizationMemberRepository.findByAccountUuid(accountId)
                        .orElseThrow(ForbiddenException::new);

        String trimmed = search == null ? null : search.trim();

        Page<InventoriesResponse> page;


        // 권한이 BOSS면 전체 저장소 의약품 조회
        if(member.getOrganizationRole() == OrganizationRole.ORG_BOSS){

            Long organizationId = member.getOrganization().getId();
            List<Storage> allStorages = storageRepository.findAllByOrganizationId(organizationId);
            List<Long> storageIds = allStorages.stream()
                    .map(s -> s.getId()).toList();


            

            page = medicineInventoryRepository.findAllInventories(search,storageId,storageIds,pageable);


            log.info("Boss : 전체 재고 조회 : {} ",page);

        }

        // 부서내 멤버면 부서에 해당하는 저장소 의약품 조회
        else{


            List<MemberDepartment> memberDepartments = memberDepartmentRepository.findAllByOrganizationMember(member);

            if(memberDepartments.isEmpty()){
                return Page.empty();
            }


            List<Long> departmentIds = memberDepartments.stream()
                    .map(md -> md.getDepartment().getId())
                    .toList();

            page = medicineInventoryRepository.findAllInventoriesByDepartmentIds(trimmed,storageId,departmentIds,pageable);

            log.info("departmentIds = {}", departmentIds);
            log.info("부서 전체 재고 조회 : {} ",page.getContent());

        }


        return page;

    }







}
