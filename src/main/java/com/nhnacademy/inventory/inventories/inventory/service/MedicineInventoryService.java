package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.inventories.inventory.dto.MedicineInboundCreateRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicineInventoryService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final ZoneRepository zoneRepository;

    //입고
    @Transactional
    public void inbound(MedicineInboundCreateRequest request){





    }




}
