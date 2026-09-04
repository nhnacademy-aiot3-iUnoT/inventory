package com.nhnacademy.inventory.inventories.inventory.operation.disposal.service;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.DisposalNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalReason;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineDisposalServiceTest {

    @Mock
    private MedicineInventoryRepository medicineInventoryRepository;

    @Mock
    private DisposalOperation disposalOperation;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MedicineDisposalService medicineDisposalService;

    @Test
    @DisplayName("폐기할 재고는 잠금 조회하고 폐기 작업을 호출한다.")
    void disposeInventory() {
        Long inventoryId = 1L;

        MedicineDisposalRequest request =
                new MedicineDisposalRequest(
                        3,
                        DisposalReason.DETERIORATED,
                        null
                );

        MedicineInventory inventory = mock(MedicineInventory.class);
        Zone zone = mock(Zone.class);
        MedicinePackageUnit packageUnit = mock(MedicinePackageUnit.class);

        when(inventory.getZone()).thenReturn(zone);
        when(inventory.getMedicinePackageUnit()).thenReturn(packageUnit);

        when(medicineInventoryRepository.findByIdForUpdate(inventoryId))
                .thenReturn(Optional.of(inventory));

        medicineDisposalService.dispose(inventoryId, request);

        verify(medicineInventoryRepository)
                .findByIdForUpdate(inventoryId);

        verify(disposalOperation)
                .process(inventory, request);
    }

    @Test
    @DisplayName("폐기할 재고가 없으면 예외가 발생한다.")
    void disposeInventory_inventoryNotFound() {
        Long inventoryId = 999L;

        MedicineDisposalRequest request =
                new MedicineDisposalRequest(
                        3,
                        DisposalReason.DETERIORATED,
                        null
                );

        when(medicineInventoryRepository.findByIdForUpdate(inventoryId))
                .thenReturn(Optional.empty());

        assertThrowsExactly(
                DisposalNotFoundException.class,
                () -> medicineDisposalService.dispose(
                        inventoryId,
                        request
                )
        );

        verify(medicineInventoryRepository)
                .findByIdForUpdate(inventoryId);

        verifyNoInteractions(disposalOperation);
    }
}