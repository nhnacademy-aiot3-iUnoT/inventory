package com.nhnacademy.inventory.inventories.inventory.operation.disposal.sevice;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.InventoryNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicineDisposalService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final DisposalOperation disposalOperation;

    @Transactional
    public void dispose(
            MedicineDisposalRequest request,
            UUID processedBy
    ) {
        MedicineInventory inventory =
                medicineInventoryRepository
                        .findByIdForUpdate(request.inventoryId())
                        .orElseThrow(InventoryNotFoundException::new);

        disposalOperation.process(
                inventory,
                request,
                processedBy
        );
    }
}
