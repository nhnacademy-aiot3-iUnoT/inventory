package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;

import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ZoneRepository zoneRepository;
    private final MedicineInventoryRepository inventoryRepository;


    @Transactional(readOnly = true)
    public Long getTotalQuantity(Long storageId, Long packUnitId){

        List<Zone> zones = zoneRepository.findAllByStorageId(storageId);

        if(zones == null || zones.isEmpty()){

            return 0L;
        }

        return inventoryRepository.findByZoneAndPackUnitSum(zones,packUnitId);


    }






}
