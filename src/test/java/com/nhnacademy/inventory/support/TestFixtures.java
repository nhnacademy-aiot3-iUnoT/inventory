package com.nhnacademy.inventory.support;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.UUID;

public class TestFixtures {

    public static Organization createOrganization() {
        return Organization.builder()
                .businessNumber("1234567890")
                .name("테스트약국")
                .roadAddress("서울시 어딘가")
                .zipCode("12345")
                .status(OrganizationStatus.ACTIVE)
                .build();
    }

    public static Storage createStorage(Organization organization, String name) {
        return Storage.builder()
                .organization(organization)
                .name(name)
                .status(StorageStatus.ACTIVE)
                .build();
    }


    public static Zone createZone(Storage storage, String name) {
        return Zone.builder()
                .storage(storage)
                .name(name)
                .status(ZoneStatus.ACTIVE)
                .envStatus(EnvStatus.NORMAL)
                .build();
    }


    public static Medicine createMedicine(String itemCode, String productName,
                                          String storageMethod, String companyName) {
        return Medicine.builder()
                .itemCode(itemCode)
                .productName(productName)
                .storageMethod(storageMethod)
                .companyName(companyName)
                .build();
    }


    public static MedicinePackageUnit createPackageUnit(Medicine medicine, String packUnit) {
        return MedicinePackageUnit.builder()
                .medicine(medicine)
                .packUnit(packUnit)
                .build();
    }

    public static MedicineInventory createInventory(MedicinePackageUnit packageUnit, Zone zone,
                                                    String lotNumber, int currentQuantity) {
        return MedicineInventory.builder()
                .medicinePackageUnit(packageUnit)
                .zone(zone)
                .lotNumber(lotNumber)
                .expirationDate(LocalDate.now().plusYears(1))
                .currentQuantity(currentQuantity)
                .managementStatus(ManagementStatus.NORMAL)
                .build();
    }

    public static StockTransaction createStockTransaction(MedicineInventory inventory,
                                                          TransactionType transactionType,
                                                          int quantity, int beforeQuantity, int afterQuantity) {
        return StockTransaction.builder()
                .medicineInventory(inventory)
                .transactionType(transactionType)
                .quantity(quantity)
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .processedBy(randomUuidBytes())
                .build();
    }

    public static byte[] randomUuidBytes() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
}
