package com.nhnacademy.inventory.inventories.inventory.domain;

import com.nhnacademy.inventory.inventories.inventory.exception.InsufficientStockException;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MedicineInventoryTest {

    @Test
    @DisplayName("일부 폐기 시 폐기한 수량만큼 재고가 감소한다")
    void disposalPartOfInventory() {
        MedicineInventory inventory = createInventory(10);

        inventory.disposeQuantity(3);

        assertEquals(7, inventory.getCurrentQuantity());
        assertEquals(
                ManagementStatus.NORMAL,
                inventory.getManagementStatus()
        );
    }

    private MedicineInventory createInventory(int quantity) {
        return MedicineInventory.create(
                mock(MedicinePackageUnit.class),
                mock(Zone.class),
                "LOT-001",
                LocalDate.now().plusYears(1),
                quantity
        );
    }

    @Test
    @DisplayName("전량 폐기 시 재고가 0이 되고 폐기 상태로 변경된다.")
    void disposalAllInventory() {
        MedicineInventory inventory = createInventory(10);
         inventory.disposeQuantity(10);

         assertEquals(0, inventory.getCurrentQuantity());
         assertEquals(
                 ManagementStatus.DISPOSAL,
                 inventory.getManagementStatus()
         );
    }

    @Test
    @DisplayName("현재 재고보다 많이 폐기하면 예외가 발생한다.")
    void cannotDisposeMoreThanInventory() {
        MedicineInventory inventory = createInventory(10);

        assertThrows(
                InsufficientStockException.class,
                () -> inventory.disposeQuantity(11)
        );
    }
}
