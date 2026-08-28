package com.nhnacademy.inventory.inventories.inventory.operation.disposal.service;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.DisposalNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalTargetResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineDisposalService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final DisposalOperation disposalOperation;

    public MedicineDisposalTargetResponse getDisposalTarget(Long inventoryId) {
        MedicineInventory inventory = medicineInventoryRepository
                .findById(inventoryId)
                .orElseThrow(DisposalNotFoundException::new);

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

        disposalOperation.process(inventory, request);
    }

}
