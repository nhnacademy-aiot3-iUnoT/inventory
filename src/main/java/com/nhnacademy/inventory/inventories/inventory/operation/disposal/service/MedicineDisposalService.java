package com.nhnacademy.inventory.inventories.inventory.operation.disposal.service;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.DisposalNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalTargetResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MedicineDisposalService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final DisposalOperation disposalOperation;

    @Transactional
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
