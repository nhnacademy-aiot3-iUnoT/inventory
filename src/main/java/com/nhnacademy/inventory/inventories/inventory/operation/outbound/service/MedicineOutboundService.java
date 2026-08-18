package com.nhnacademy.inventory.inventories.inventory.operation.outbound.service;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicineOutboundService {
    private final MedicineInventoryRepository medicineInventoryRepository;
    private final ZoneRepository zoneRepository;
    private final OutboundOperation outboundOperation;

    @Transactional
    public void outbound(MedicineOutboundRequest request,
                         UUID processBy
    ) {

        Zone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(ZoneNotFoundException::new);

        List<MedicineInventory> inventories =
                medicineInventoryRepository
                        .findOutboundInventories(
                                request.medicinePackageUnitId(),
                                request.zoneId()
                        );

        outboundOperation.process(
                zone,
                inventories,
                request,
                processBy
        );
    }



}
