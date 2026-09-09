package com.nhnacademy.inventory.inventories.inventory.operation.inbound.domain;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InboundOperationTest {

    @Mock
    MedicineInventoryRepository medicineInventoryRepository;
    @Mock
    StockTransactionService stockTransactionService;

    @InjectMocks
    InboundOperation inboundOperation;

    MedicinePackageUnit medicinePackageUnit;
    Zone zone;
    MedicineInventory medicineInventory;
    MedicineInboundRequest request;



    @BeforeEach
    void setUp(){

        Medicine medicine = Medicine.create(
                "123456789",
                "product-test",
                "storage-test",
                "validity-test",
                null,
                "company-test"

        );

        medicinePackageUnit = MedicinePackageUnit.create(
                medicine,
                "10ml");

        zone = Zone.builder()
                .name("zone-test")
                .description("description-test")
                .status(ZoneStatus.ACTIVE)
                .envStatus(EnvStatus.NORMAL)
                .build();

        medicineInventory = MedicineInventory.create(
                medicinePackageUnit,
                zone,
                "ABC-123",
                LocalDate.now(),
                20
        );

        request = new MedicineInboundRequest(
                1L,
                1L,
                "ABC-123",
                LocalDate.now().plusDays(1),
                20,
                null,
                TransactionType.INBOUND,
                null
        );




    }

    @Test
    @DisplayName("inventory가 null인 경우 새 의약품 입고 등록")
    void inboundInventory() {

        inboundOperation.inboundInventory(
                medicinePackageUnit,
                zone,
                null,
                request

        );

        ArgumentCaptor<MedicineInventory> captor = ArgumentCaptor.forClass(MedicineInventory.class);
        verify(medicineInventoryRepository).save(captor.capture());

        MedicineInventory inventory = captor.getValue();


        assertAll(
                () -> assertEquals(zone,inventory.getZone()),
                () -> assertEquals(medicinePackageUnit,inventory.getMedicinePackageUnit()),
                () -> assertEquals("ABC-123",inventory.getLotNumber()),
                () -> assertEquals(20,inventory.getCurrentQuantity()),
                () -> assertEquals(ManagementStatus.NORMAL,inventory.getManagementStatus())
        );


        verify(medicineInventoryRepository).save(any(MedicineInventory.class));
        verify(stockTransactionService).createStockTransaction(any(StockTransactionCommand.class));



    }

    @Test
    @DisplayName("inventory가 있는 경우 의약품 수량 증가")
    void isInventoryTest(){

        inboundOperation.inboundInventory(
                medicinePackageUnit,
                zone,
                medicineInventory,
                request

        );


        assertEquals(40,medicineInventory.getCurrentQuantity());


    }











}