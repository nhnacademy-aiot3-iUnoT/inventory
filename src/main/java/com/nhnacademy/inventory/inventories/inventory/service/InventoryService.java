package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.domain.InventoryGroupKey;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import com.nhnacademy.inventory.organizations.department.exception.MemberDepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {


    private final OrganizationMemberRepository memberRepository;
    private final MemberDepartmentRepository memberDepartmentRepository;
    private final DepartmentRepository departmentRepository;
    private final StorageDepartmentRepository storageDepartmentRepository;
    private final ZoneRepository zoneRepository;
    private final MedicineInventoryRepository inventoryRepository;


//    // 전체 재고 조회
//    @Transactional(readOnly = true)
//    public List<InventoriesResponse> getInventories(Pageable pageable){
//
//        UUID accountId = UserContext.getUserUuid();
//        OrganizationMember organizationMember = memberRepository.findByAccountUuid(accountId)
//                .orElseThrow(UserOrgNotFoundException::new);
//
//        // uuid로 조직원 , 부서들 조회
//        List<MemberDepartment> memberDepartments = memberDepartmentRepository.findAllByOrganizationMemberId(organizationMember.getId());
//
//        if(memberDepartments.isEmpty()){
//            throw new MemberDepartmentNotFoundException();
//        }
//
//        // 해당하는 부서들에 맞는 저장소-부서 조회
//        List<StorageDepartment> storageDepartments =
//                memberDepartments.stream()
//                        .map(md -> (storageDepartmentRepository.findByDepartmentId(
//                                md.getDepartment().getId()).orElse(null)
//                        )).toList();
//
//        // 저장소가 없다? 그럼 빈 리스트 반환
//        if(storageDepartments.isEmpty()){
//            return List.of();
//        }
//
//        // 부서에 해당하는 저장소 전체 조회
//        List<Storage> storages = storageDepartments.stream()
//                .map(
//                        sd -> sd.getStorage()
//                ).toList();
//
//        // 저장소에 해당하는 전체 구역 조회
//        List<Zone> zones =
//                storages.stream()
//                        .flatMap(s ->
//                                zoneRepository.findAllByStorage(s).stream()
//                                ).toList();
//
//        // 조직원 -> 부서 -> 저장소->  구역에 해당하는 의약품 검색
//        List<MedicineInventory> medicineInventories = inventoryRepository.findAllByZoneIn(zones);
//
//        // 저장소-구역에 해당하는 의약품이 없으면 빈 리스트
//        if(medicineInventories.isEmpty()){
//            return List.of();
//        }
//
//        // 저장소와 단위의약품으로 묶음
//        Map<InventoryGroupKey,List<MedicineInventory>> group = new HashMap<>();
//
//        for(MedicineInventory medicineInventory : medicineInventories){
//
//            InventoryGroupKey key = new InventoryGroupKey(
//                    medicineInventory.getZone().getStorage().getId(),
//                    medicineInventory.getMedicinePackageUnit().getId()
//            );
//
//            if(!group.containsKey(key)){
//                group.put(key,new ArrayList<>());
//            }
//
//            group.get(key).add(medicineInventory);
//
//        }
//
//        List<InventoriesResponse> inventories = new ArrayList<>();
//
//        // key 그룹화하여 의약품들 InventoriesResponse로 나타냄
//        for(List<MedicineInventory> list : group.values()){
//            inventories.add(createResponse(list));
//
//        }
//
//        log.info("inventories: {}",inventories);
//
//
//        return inventories;
//
//    }
//
//    private InventoriesResponse createResponse(List<MedicineInventory> groupedList){
//
//        int totalQuantity = 0;
//        MedicineInventory near = groupedList.getFirst();
//
//        for(MedicineInventory medicineInventory : groupedList){
//            // 그룹핑 전체 수량 합산
//            totalQuantity += medicineInventory.getCurrentQuantity();
//
//
//            // 유통기한 임박 기준으로 대표로 의약품 제조번호, 유통기한을 보여줌
//            if(near.getExpirationDate().isAfter(medicineInventory.getExpirationDate())){
//                near = medicineInventory;
//            }
//
//        }
//
//        MedicinePackageUnit medicinePackageUnit = near.getMedicinePackageUnit();
//        Medicine medicine = medicinePackageUnit.getMedicine();
//
//        InventoriesResponse response =
//                InventoriesResponse.from(
//                        medicine.getProductName(),
//                        medicine.getItemCode(),
//                        medicinePackageUnit.getPackUnit(),
//                        near.getExpirationDate(),
//                        near.getZone().getStorage().getName(),
//                        totalQuantity
//
//                );
//
//        log.info("inventory response: {}",response);
//
//        return response;
//
//
//    }




}
