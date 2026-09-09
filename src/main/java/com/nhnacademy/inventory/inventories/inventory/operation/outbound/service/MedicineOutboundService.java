package com.nhnacademy.inventory.inventories.inventory.operation.outbound.service;

import com.nhnacademy.inventory.assistant.event.StockOutboundInspectionEvent;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.alert.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.InsufficientStockException;
import com.nhnacademy.inventory.inventories.inventory.exception.InventoryNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.operation.InventoryOperationAccessValidator;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundTargetResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineOutboundService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final InventoryOperationAccessValidator accessValidator;
    private final OutboundOperation outboundOperation;
    private final ApplicationEventPublisher eventPublisher;

    public MedicineOutboundTargetResponse getOutboundTarget(Long inventoryId) {
        MedicineInventory inventory = medicineInventoryRepository
                .findById(inventoryId)
                .orElseThrow(InventoryNotFoundException::new);

        validateAccess(inventory);

        return MedicineOutboundTargetResponse.from(
                inventory,
                getAvailableQuantity(inventory)
        );
    }

    @Transactional
    public void outbound(Long inventoryId, MedicineOutboundRequest request) {
        MedicineInventory inventory = medicineInventoryRepository
                .findByIdForUpdate(inventoryId)
                .orElseThrow(InventoryNotFoundException::new);

        validateAccess(inventory);

        int availableQuantity = getAvailableQuantity(inventory);

        if (request.quantity() == null
                || request.quantity() <= 0
                || request.quantity() > availableQuantity) {
            throw new InsufficientStockException();
        }

        outboundOperation.process(
                inventory.getZone(),
                List.of(inventory),
                request
        );

        eventPublisher.publishEvent(new StockOutboundCompletedEvent(
                inventory.getZone().getId(),
                inventory.getMedicinePackageUnit().getId()
        ));

        eventPublisher.publishEvent(new StockOutboundInspectionEvent(
                UserContext.getUserUuid(),
                inventory.getZone().getId(),
                inventory.getMedicinePackageUnit().getId(),
                request.quantity()
        ));
    }

    private void validateAccess(MedicineInventory inventory) {
        accessValidator.validate(
                inventory.getZone(),
                inventory.getMedicinePackageUnit().getMedicine()
        );
    }

    private int getAvailableQuantity(MedicineInventory inventory) {
        if (inventory.getManagementStatus() != ManagementStatus.NORMAL
                || inventory.getExpirationDate().isBefore(LocalDate.now())
                || inventory.getCurrentQuantity() <= 0) {
            return 0;
        }

        return inventory.getCurrentQuantity();
    }
}