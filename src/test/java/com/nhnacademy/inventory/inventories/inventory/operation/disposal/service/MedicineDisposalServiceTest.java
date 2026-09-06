package com.nhnacademy.inventory.inventories.inventory.operation.disposal.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.DisposalNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.exception.InvalidExpiredDisposalTargetException;
import com.nhnacademy.inventory.inventories.inventory.operation.InventoryOperationAccessValidator;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalReason;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.ExpiredInventoryDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineDisposalServiceTest {

    @Mock
    private InventoryOperationAccessValidator accessValidator;

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
        Storage storage = mock(Storage.class);
        Medicine medicine = mock(Medicine.class);

        when(inventory.getZone()).thenReturn(zone);
        when(zone.getStorage()).thenReturn(storage);
        when(inventory.getMedicinePackageUnit())
                .thenReturn(packageUnit);
        when(packageUnit.getMedicine()).thenReturn(medicine);
        when(medicineInventoryRepository.findByIdForUpdate(inventoryId))
                .thenReturn(Optional.of(inventory));

        medicineDisposalService.dispose(inventoryId, request);

        verify(accessValidator).validate(storage, medicine);
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

        verifyNoInteractions(
                accessValidator,
                disposalOperation
        );
    }

    @Test
    @DisplayName("선택한 만료 재고를 ID 순으로 잠그고 전량 폐기한다.")
    void disposeExpiredInventories_success() {
        InventoryFixture first =
                createExpiredInventory(
                        ManagementStatus.NORMAL,
                        3
                );

        InventoryFixture second =
                createExpiredInventory(
                        ManagementStatus.UNDER_REVIEW,
                        5
                );

        ExpiredInventoryDisposalRequest request =
                new ExpiredInventoryDisposalRequest(
                        List.of(2L, 1L)
                );

        when(medicineInventoryRepository.findAllByIdsForUpdate(
                List.of(1L, 2L)
        )).thenReturn(
                List.of(
                        first.inventory(),
                        second.inventory()
                )
        );

        medicineDisposalService.disposeExpiredInventories(request);

        verify(medicineInventoryRepository)
                .findAllByIdsForUpdate(List.of(1L, 2L));

        InOrder order =
                inOrder(
                        accessValidator,
                        disposalOperation
                );

        order.verify(accessValidator).validate(
                first.storage(),
                first.medicine()
        );

        order.verify(accessValidator).validate(
                second.storage(),
                second.medicine()
        );

        order.verify(disposalOperation).process(
                first.inventory(),
                new MedicineDisposalRequest(
                        3,
                        DisposalReason.EXPIRED,
                        null
                )
        );

        order.verify(disposalOperation).process(
                second.inventory(),
                new MedicineDisposalRequest(
                        5,
                        DisposalReason.EXPIRED,
                        null
                )
        );
    }

    @Test
    @DisplayName("선택한 ID 중 존재하지 않는 재고가 있으면 모두 처리하지 않는다.")
    void disposeExpiredInventories_inventoryNotFound() {
        ExpiredInventoryDisposalRequest request =
                new ExpiredInventoryDisposalRequest(
                        List.of(1L, 999L)
                );

        when(medicineInventoryRepository.findAllByIdsForUpdate(
                List.of(1L, 999L)
        )).thenReturn(
                List.of(mock(MedicineInventory.class))
        );

        assertThrowsExactly(
                DisposalNotFoundException.class,
                () -> medicineDisposalService
                        .disposeExpiredInventories(request)
        );

        verifyNoInteractions(
                accessValidator,
                disposalOperation
        );
    }

    @Test
    @DisplayName("유통기한이 경과하지 않은 재고가 포함되면 모두 처리하지 않는다.")
    void disposeExpiredInventories_notExpired() {
        MedicineInventory inventory =
                mock(MedicineInventory.class);

        when(inventory.getExpirationDate())
                .thenReturn(LocalDate.now());

        when(inventory.getManagementStatus())
                .thenReturn(ManagementStatus.NORMAL);

        when(inventory.getCurrentQuantity())
                .thenReturn(3);

        when(medicineInventoryRepository.findAllByIdsForUpdate(
                List.of(1L)
        )).thenReturn(List.of(inventory));

        ExpiredInventoryDisposalRequest request =
                new ExpiredInventoryDisposalRequest(
                        List.of(1L)
                );

        assertThrowsExactly(
                InvalidExpiredDisposalTargetException.class,
                () -> medicineDisposalService
                        .disposeExpiredInventories(request)
        );

        verifyNoInteractions(
                accessValidator,
                disposalOperation
        );
    }

    @Test
    @DisplayName("한 재고라도 권한 검증에 실패하면 어떤 재고도 폐기하지 않는다.")
    void disposeExpiredInventories_forbidden() {
        InventoryFixture first =
                createExpiredInventory(
                        ManagementStatus.NORMAL,
                        3
                );

        InventoryFixture second =
                createExpiredInventory(
                        ManagementStatus.NORMAL,
                        5
                );

        when(medicineInventoryRepository.findAllByIdsForUpdate(
                List.of(1L, 2L)
        )).thenReturn(
                List.of(
                        first.inventory(),
                        second.inventory()
                )
        );

        doNothing()
                .when(accessValidator)
                .validate(
                        first.storage(),
                        first.medicine()
                );

        doThrow(new ForbiddenException())
                .when(accessValidator)
                .validate(
                        second.storage(),
                        second.medicine()
                );

        ExpiredInventoryDisposalRequest request =
                new ExpiredInventoryDisposalRequest(
                        List.of(1L, 2L)
                );

        assertThrowsExactly(
                ForbiddenException.class,
                () -> medicineDisposalService
                        .disposeExpiredInventories(request)
        );

        verify(accessValidator).validate(
                first.storage(),
                first.medicine()
        );

        verify(accessValidator).validate(
                second.storage(),
                second.medicine()
        );

        verifyNoInteractions(disposalOperation);
    }

    private InventoryFixture createExpiredInventory(
            ManagementStatus managementStatus,
            int currentQuantity
    ) {
        MedicineInventory inventory =
                mock(MedicineInventory.class);

        Zone zone = mock(Zone.class);
        Storage storage = mock(Storage.class);

        MedicinePackageUnit packageUnit =
                mock(MedicinePackageUnit.class);

        Medicine medicine = mock(Medicine.class);

        when(inventory.getExpirationDate())
                .thenReturn(LocalDate.now().minusDays(1));

        when(inventory.getManagementStatus())
                .thenReturn(managementStatus);

        when(inventory.getCurrentQuantity())
                .thenReturn(currentQuantity);

        when(inventory.getZone())
                .thenReturn(zone);

        when(zone.getStorage())
                .thenReturn(storage);

        when(inventory.getMedicinePackageUnit())
                .thenReturn(packageUnit);

        when(packageUnit.getMedicine())
                .thenReturn(medicine);

        return new InventoryFixture(
                inventory,
                storage,
                medicine
        );
    }

    private record InventoryFixture(
            MedicineInventory inventory,
            Storage storage,
            Medicine medicine
    ) {
    }
}