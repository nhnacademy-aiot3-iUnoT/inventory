package com.nhnacademy.inventory.inventories.inventory.operation.outbound.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.inventories.alert.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.InsufficientStockException;
import com.nhnacademy.inventory.inventories.inventory.operation.InventoryOperationAccessValidator;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicineOutboundServiceTest {

    private MedicineInventoryRepository repository;
    private InventoryOperationAccessValidator accessValidator;
    private StockTransactionService transactionService;
    private ApplicationEventPublisher eventPublisher;
    private MedicineOutboundService service;

    private MedicinePackageUnit packageUnit;
    private Zone zone;
    private Storage storage;
    private Medicine medicine;
    private MedicineInventory selected;
    private MedicineInventory earlier;

    @BeforeEach
    void setUp() {
        repository = mock(MedicineInventoryRepository.class);
        accessValidator = mock(InventoryOperationAccessValidator.class);
        transactionService = mock(StockTransactionService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new MedicineOutboundService(
                repository,
                accessValidator,
                new OutboundOperation(transactionService),
                eventPublisher
        );

        packageUnit = mock(MedicinePackageUnit.class);
        zone = mock(Zone.class);
        storage = mock(Storage.class);
        medicine = mock(Medicine.class);

        when(zone.getId()).thenReturn(20L);
        when(zone.getStorage()).thenReturn(storage);
        when(packageUnit.getId()).thenReturn(10L);
        when(packageUnit.getMedicine()).thenReturn(medicine);

        selected = MedicineInventory.create(
                packageUnit, zone, "selected-lot",
                LocalDate.now().plusDays(20), 20
        );
        earlier = MedicineInventory.create(
                packageUnit, zone, "earlier-lot",
                LocalDate.now().plusDays(10), 20
        );
        when(repository.findByIdForUpdate(75L))
                .thenReturn(Optional.of(selected));
        when(repository.findById(75L))
                .thenReturn(Optional.of(selected));
    }

    @Test
    @DisplayName("선택한 재고만 차감한다")
    void outbound_decreasesSelectedInventoryOnly() {
        service.outbound(75L, request(10));

        assertEquals(10, selected.getCurrentQuantity());
        assertEquals(20, earlier.getCurrentQuantity());

        verify(accessValidator).validate(zone, medicine);
        verify(repository).findByIdForUpdate(75L);
        verify(repository, never())
                .findOutboundInventories(anyLong(), anyLong());
        verify(transactionService)
                .createStockTransaction(any(StockTransactionCommand.class));
        verify(eventPublisher)
                .publishEvent(new StockOutboundCompletedEvent(20L, 10L));
    }

    @Test
    @DisplayName("선택한 재고 수량을 초과하면 다른 LOT에서 차감하지 않고 거절한다")
    void outbound_rejectsQuantityExceedingSelectedStock() {
        assertThrowsExactly(
                InsufficientStockException.class,
                () -> service.outbound(75L, request(30))
        );

        assertEquals(20, selected.getCurrentQuantity());
        assertEquals(20, earlier.getCurrentQuantity());
        verifyNoInteractions(transactionService, eventPublisher);
    }

    @Test
    @DisplayName("전량 출고하면 선택한 재고만 소진 상태로 변경한다")
    void outbound_depletesSelectedInventoryWhenAllStockIsUsed() {
        service.outbound(75L, request(20));

        assertEquals(0, selected.getCurrentQuantity());
        assertEquals(ManagementStatus.DEPLETED, selected.getManagementStatus());
        assertEquals(20, earlier.getCurrentQuantity());
        assertEquals(ManagementStatus.NORMAL, earlier.getManagementStatus());
    }

    @Test
    @DisplayName("출고 가능 수량은 선택한 재고의 수량으로 조회한다")
    void getOutboundTarget_returnsSelectedStockQuantity() {
        assertEquals(20, service.getOutboundTarget(75L).availableQuantity());

        verify(repository, never())
                .sumAvailableQuantity(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("환경 검토 중인 재고는 출고할 수 없다")
    void outbound_rejectsInventoryUnderReview() {
        selected.setManagementStatus(ManagementStatus.UNDER_REVIEW);

        assertEquals(0, service.getOutboundTarget(75L).availableQuantity());
        assertThrowsExactly(
                InsufficientStockException.class,
                () -> service.outbound(75L, request(1))
        );
        assertEquals(20, selected.getCurrentQuantity());
        verifyNoInteractions(transactionService, eventPublisher);
    }

    @Test
    @DisplayName("유통기한이 지난 재고는 출고할 수 없다")
    void outbound_rejectsExpiredInventory() {
        MedicineInventory expired = MedicineInventory.create(
                packageUnit, zone, "expired-lot",
                LocalDate.now().minusDays(1), 20
        );
        when(repository.findByIdForUpdate(75L))
                .thenReturn(Optional.of(expired));

        assertThrowsExactly(
                InsufficientStockException.class,
                () -> service.outbound(75L, request(1))
        );
        assertEquals(20, expired.getCurrentQuantity());
        verifyNoInteractions(transactionService, eventPublisher);
    }

    @Test
    @DisplayName("권한이 없으면 출고를 거절한다")
    void outbound_rejectsUnauthorizedAccess() {
        doThrow(new ForbiddenException())
                .when(accessValidator).validate(zone, medicine);

        assertThrowsExactly(
                ForbiddenException.class,
                () -> service.outbound(75L, request(1))
        );
        assertEquals(20, selected.getCurrentQuantity());
        verifyNoInteractions(transactionService, eventPublisher);
    }

    @Test
    @DisplayName("권한이 없으면 출고 대상 조회를 거절한다")
    void getOutboundTarget_rejectsUnauthorizedAccess() {
        doThrow(new ForbiddenException())
                .when(accessValidator).validate(zone, medicine);

        assertThrowsExactly(
                ForbiddenException.class,
                () -> service.getOutboundTarget(75L)
        );
    }

    private MedicineOutboundRequest request(int quantity) {
        return new MedicineOutboundRequest(
                10L, quantity, 20L, OutboundReason.DISPENSING, null
        );
    }
}