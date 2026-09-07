package com.nhnacademy.inventory.inventories.inventory.operation.outbound.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.inventories.alert.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.operation.InventoryOperationAccessValidator;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineOutboundServiceTest {

    @Mock
    private MedicineInventoryRepository medicineInventoryRepository;

    @Mock
    private MedicinePackageUnitRepository medicinePackageUnitRepository;

    @Mock
    private InventoryOperationAccessValidator accessValidator;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private OutboundOperation outboundOperation;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MedicineOutboundService medicineOutboundService;

    @Test
    void 출고할_때_권한을_검증하고_출고_작업을_호출한다() {
        MedicineOutboundRequest request = new MedicineOutboundRequest(
                10L,
                3,
                20L,
                OutboundReason.DISPENSING,
                null
        );

        Zone zone = mock(Zone.class);
        Storage storage = mock(Storage.class);
        MedicinePackageUnit packageUnit = mock(MedicinePackageUnit.class);
        Medicine medicine = mock(Medicine.class);
        List<MedicineInventory> inventories =
                List.of(mock(MedicineInventory.class));

        when(zoneRepository.findById(20L))
                .thenReturn(Optional.of(zone));
        when(medicinePackageUnitRepository.findById(10L))
                .thenReturn(Optional.of(packageUnit));
        when(zone.getStorage()).thenReturn(storage);
        when(packageUnit.getMedicine()).thenReturn(medicine);
        when(medicineInventoryRepository.findOutboundInventories(10L, 20L))
                .thenReturn(inventories);

        medicineOutboundService.outbound(request);

        verify(accessValidator).validate(storage, medicine);
        verify(medicineInventoryRepository)
                .findOutboundInventories(10L, 20L);
        verify(eventPublisher).publishEvent(
                new StockOutboundCompletedEvent(20L, 10L)
        );
        verify(outboundOperation)
                .process(zone, inventories, request);
    }

    @Test
    void 권한이_없으면_출고_재고를_조회하거나_처리하지_않는다() {
        MedicineOutboundRequest request = new MedicineOutboundRequest(
                10L,
                3,
                20L,
                OutboundReason.DISPENSING,
                null
        );

        Zone zone = mock(Zone.class);
        Storage storage = mock(Storage.class);
        MedicinePackageUnit packageUnit = mock(MedicinePackageUnit.class);
        Medicine medicine = mock(Medicine.class);

        when(zoneRepository.findById(20L))
                .thenReturn(Optional.of(zone));
        when(medicinePackageUnitRepository.findById(10L))
                .thenReturn(Optional.of(packageUnit));
        when(zone.getStorage()).thenReturn(storage);
        when(packageUnit.getMedicine()).thenReturn(medicine);

        doThrow(new ForbiddenException())
                .when(accessValidator)
                .validate(storage, medicine);

        assertThrowsExactly(
                ForbiddenException.class,
                () -> medicineOutboundService.outbound(request)
        );

        verifyNoInteractions(outboundOperation, eventPublisher);
        verifyNoInteractions(outboundOperation);
    }

    @Test
    void 출고_대상_조회도_권한이_없으면_차단한다() {
        MedicineInventory inventory = mock(MedicineInventory.class);
        Zone zone = mock(Zone.class);
        Storage storage = mock(Storage.class);
        MedicinePackageUnit packageUnit = mock(MedicinePackageUnit.class);
        Medicine medicine = mock(Medicine.class);

        when(medicineInventoryRepository.findById(1L))
                .thenReturn(Optional.of(inventory));
        when(inventory.getZone()).thenReturn(zone);
        when(zone.getStorage()).thenReturn(storage);
        when(inventory.getMedicinePackageUnit()).thenReturn(packageUnit);
        when(packageUnit.getMedicine()).thenReturn(medicine);

        doThrow(new ForbiddenException())
                .when(accessValidator)
                .validate(storage, medicine);

        assertThrowsExactly(
                ForbiddenException.class,
                () -> medicineOutboundService.getOutboundTarget(1L)
        );

        verify(medicineInventoryRepository, never())
                .sumAvailableQuantity(anyLong(), anyLong(), any());
    }
}
