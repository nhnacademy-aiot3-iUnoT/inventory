package com.nhnacademy.inventory.inventories.inventory.operation.inbound.service;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.domain.InboundOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.exception.MedicineNotFoundException;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InboundService {


    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final ZoneRepository zoneRepository;
    private final MedicineInventoryRepository medicineInventoryRepository;
    private final InboundOperation inboundOperation;


    // 입고 등록
    @Transactional
    public void createInbound(MedicineInboundRequest request){


        MedicinePackageUnit medicinePackageUnit = medicinePackageUnitRepository.findById(request.medicinePackageUnitId())
                .orElseThrow(MedicineNotFoundException::new);
        Zone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(ZoneNotFoundException::new);

        MedicineInventory medicineInventory = medicineInventoryRepository
                .findByMedicinePackageUnitIdAndZoneIdAndLotNumberAndExpirationDate(
                        medicinePackageUnit.getId(),
                        zone.getId(),
                        request.lotNumber().trim(),
                        request.expirationDate()
                        )
                .orElse(null);


        // 입고 처리
        inboundOperation.inboundInventory(medicinePackageUnit,
                zone,
                medicineInventory,
                request);


    }





}
