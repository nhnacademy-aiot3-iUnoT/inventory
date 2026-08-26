package com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisposalOperationTest {

    @Mock
    private StockTransactionService stockTransactionService;

    @InjectMocks
    private DisposalOperation disposalOperation;

    @Test
    @DisplayName("재고를 폐기하고 폐기 이력을 생성한다.")
    void process() {
        MedicineInventory inventory = mock(MedicineInventory.class);
        MedicinePackageUnit medicinePackageUnit = mock(MedicinePackageUnit.class);
        Zone zone = mock(Zone.class);

        UUID processedBy = UUID.randomUUID();
        UserContext.setUserUuid(processedBy);

        MedicineDisposalRequest request =
                new MedicineDisposalRequest(3, DisposalReason.OTHER, "운반 중 변질 확인");

        when(inventory.getMedicinePackageUnit()).thenReturn(medicinePackageUnit);
        when(inventory.getZone()).thenReturn(zone);

        disposalOperation.process(inventory, request);

        verify(inventory).disposeQuantity(3);

        ArgumentCaptor<StockTransactionCommand> captor = ArgumentCaptor.forClass(StockTransactionCommand.class);

        verify(stockTransactionService).createStockTransaction(captor.capture());

        StockTransactionCommand command = captor.getValue();

        assertAll(
                () -> assertSame(
                        medicinePackageUnit,
                        command.medicinePackageUnit()
                ),
                () -> assertSame(zone, command.zone()),
                () -> assertEquals(
                        TransactionType.DISPOSAL,
                        command.transactionType()
                ),
                () -> assertEquals(3, command.quantity()),
                () -> assertEquals("OTHER", command.reason()),
                () -> assertEquals(
                        "운반 중 변질 확인",
                        command.memo()
                ),
                () -> assertEquals(
                        processedBy,
                        command.processedBy()
                )
        );
    }
}
