package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryInfoResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;

import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepositoryImpl;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryService {

    private final ZoneRepository zoneRepository;
    private final MedicineInventoryRepository inventoryRepository;



    public Long getTotalQuantity(Long storageId, Long packUnitId){

        List<Zone> zones = zoneRepository.findAllByStorageId(storageId);

        if(zones == null || zones.isEmpty()){

            return 0L;
        }

        return inventoryRepository.findByZoneAndPackUnitSum(zones,packUnitId);

    }


    // 상세 재고 조회
    public PageResponse<InventoryInfoResponse> getInventoryInfo(Long storageId, Long packUnitId, Pageable pageable){

        List<Zone> zones = zoneRepository.findAllByStorageId(storageId);

        List<Long> zoneIds = zones.stream()
                        .map(z -> z.getId()).toList();


       Page<InventoryInfoResponse> result = inventoryRepository.findByZonesAndPackUnitId(zoneIds,packUnitId,pageable);

       log.info("inventory response list{}",result.getContent());


        return PageResponse.from(result);

    }



}
