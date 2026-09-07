package com.nhnacademy.inventory.inventories.inventory.operation.disposal.service;

import com.nhnacademy.inventory.inventories.alert.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.exception.InvalidExpiredDisposalTargetException;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalReason;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.ExpiredInventoryDisposalRequest;

import java.time.LocalDate;
import java.util.List;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.DisposalNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.operation.InventoryOperationAccessValidator;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalTargetResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineDisposalService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final DisposalOperation disposalOperation;
    private final ApplicationEventPublisher eventPublisher;
    private final InventoryOperationAccessValidator accessValidator;

    public MedicineDisposalTargetResponse getDisposalTarget(Long inventoryId) {
        MedicineInventory inventory = medicineInventoryRepository
                .findById(inventoryId)
                .orElseThrow(DisposalNotFoundException::new);

        accessValidator.validate(
                inventory.getZone().getStorage(),
                inventory.getMedicinePackageUnit().getMedicine()
        );

        return MedicineDisposalTargetResponse.from(inventory);
    }

    @Transactional
    public void dispose(
            Long inventoryId,
            MedicineDisposalRequest request
    ) {
        MedicineInventory inventory = medicineInventoryRepository
                .findByIdForUpdate(inventoryId)
                .orElseThrow(DisposalNotFoundException::new);

        accessValidator.validate(
                inventory.getZone().getStorage(),
                inventory.getMedicinePackageUnit().getMedicine()
        );

        disposalOperation.process(inventory, request);

        eventPublisher.publishEvent(new StockOutboundCompletedEvent(
                inventory.getZone().getId(),
                inventory.getMedicinePackageUnit().getId()
        ));
    }

    @Transactional
    public void disposeExpiredInventories(
            ExpiredInventoryDisposalRequest request
    ) {
        List<Long> inventoryIds = request.inventoryIds()
                .stream()
                .sorted()
                .toList();

        List<MedicineInventory> inventories =
                medicineInventoryRepository.findAllByIdsForUpdate(
                        inventoryIds
                );

        if (inventories.size() != inventoryIds.size()) {
            throw new DisposalNotFoundException();
        }

        LocalDate today = LocalDate.now();

        for (MedicineInventory inventory : inventories) {
            validateExpiredDisposalTarget(inventory, today);

            accessValidator.validate(
                    inventory.getZone().getStorage(),
                    inventory.getMedicinePackageUnit().getMedicine()
            );
        }

        for (MedicineInventory inventory : inventories) {
            MedicineDisposalRequest disposalRequest =
                    new MedicineDisposalRequest(
                            inventory.getCurrentQuantity(),
                            DisposalReason.EXPIRED,
                            null
                    );

            disposalOperation.process(
                    inventory,
                    disposalRequest
            );
        }
    }

    private void validateExpiredDisposalTarget(
            MedicineInventory inventory,
            LocalDate today
    ) {
        boolean expired =
                inventory.getExpirationDate().isBefore(today);

        boolean active =
                inventory.getManagementStatus() == ManagementStatus.NORMAL
                        || inventory.getManagementStatus()
                        == ManagementStatus.UNDER_REVIEW;

        boolean hasQuantity =
                inventory.getCurrentQuantity() > 0;

        if (!expired || !active || !hasQuantity) {
            throw new InvalidExpiredDisposalTargetException();
        }
    }

}
