package com.nhnacademy.inventory.inventories.inventory.operation.outbound.service;

import com.nhnacademy.inventory.inventories.alert.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.exception.InventoryNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundTargetResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineOutboundService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final ZoneRepository zoneRepository;
    private final OutboundOperation outboundOperation;
    private final ApplicationEventPublisher eventPublisher;

    public MedicineOutboundTargetResponse getOutboundTarget(
            Long inventoryId
    ) {
        MedicineInventory selectedInventory =
                medicineInventoryRepository.findById(inventoryId)
                        .orElseThrow(InventoryNotFoundException::new);

        Long medicinePackageUnitId =
                selectedInventory.getMedicinePackageUnit().getId();

        Long zoneId = selectedInventory.getZone().getId();

        int availableQuantity = Math.toIntExact(
                medicineInventoryRepository.sumAvailableQuantity(
                        medicinePackageUnitId,
                        zoneId,
                        ManagementStatus.NORMAL
                )
        );

        return MedicineOutboundTargetResponse.from(
                selectedInventory,
                availableQuantity
        );
    }

    @Transactional
    public void outbound(MedicineOutboundRequest request) {
        Zone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(ZoneNotFoundException::new);

        List<MedicineInventory> inventories =
                medicineInventoryRepository.findOutboundInventories(
                        request.medicinePackageUnitId(),
                        request.zoneId()
                );

        outboundOperation.process(
                zone,
                inventories,
                request
        );

        eventPublisher.publishEvent(new StockOutboundCompletedEvent(
                request.zoneId(),
                request.medicinePackageUnitId()
        ));
    }
}
