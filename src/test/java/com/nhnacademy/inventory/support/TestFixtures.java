package com.nhnacademy.inventory.support;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.zone.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TestFixtures {

    public static Organization createOrganization(String name, String businessNumber) {
        return Organization.create(businessNumber, name);
    }

    public static OrganizationMember createOrganizationMember(Organization organization) {
        return createOrganizationMember(organization, true);
    }

    public static OrganizationMember createOrganizationMember(Organization organization, boolean approved) {
        return OrganizationMember.builder()
                .organization(organization)
                .accountUuid(UUID.randomUUID())
                .organizationRole(OrganizationRole.ORG_MEMBER)
                .isApproved(approved)
                .build();
    }

    public static Storage createStorage(Organization organization) {
        return createStorage(organization, "본관창고");
    }

    public static Storage createStorage(Organization organization, String name) {
        return Storage.builder()
                .organization(organization)
                .name(name)
                .status(StorageStatus.ACTIVE)
                .build();
    }

    public static Zone createZone(Storage storage) {
        return createZone(storage, "테스트구역");
    }

    public static Zone createZone(Storage storage, String name) {
        return Zone.builder()
                .storage(storage)
                .name(name)
                .status(ZoneStatus.ACTIVE)
                .envStatus(EnvStatus.NORMAL)
                .build();
    }

    public static SensorType createSensorType(String name){
        return createSensorType(name, "테스트 설명");
    }

    public static SensorType createSensorType(String name, String description){
        return SensorType.builder()
                .name(name)
                .description(description)
                .build();
    }

    public static ZoneThreshold createThreshold(Zone zone, SensorType sensorType,
                                                 BigDecimal minvalue, BigDecimal maxvalue,
                                                 Integer alertDuration){
        return ZoneThreshold.builder()
                .zone(zone)
                .sensorType(sensorType)
                .minValue(minvalue)
                .maxValue(maxvalue)
                .alertDuration(alertDuration)
                .build();
    }

    public static Medicine createMedicine() {
        return createMedicine("123456789", "타이레놀");
    }

    public static Medicine createMedicine(String itemCode, String productName) {
        return Medicine.builder()
                .itemCode(itemCode)
                .productName(productName)
                .storageMethod("실온보관")
                .companyName("한국얀센")
                .build();
    }

    public static MedicinePackageUnit createPackageUnit(Medicine medicine) {
        return createPackageUnit(medicine, "100정");
    }

    public static MedicinePackageUnit createPackageUnit(Medicine medicine, String packUnit) {
        return MedicinePackageUnit.create(medicine,packUnit);
    }

    public static MedicineInventory createInventory(MedicinePackageUnit packageUnit, Zone zone) {
        return createInventory(packageUnit, zone, "LOT001", 100);
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

    public static StockTransaction createStockTransaction(MedicinePackageUnit packageUnit, Zone zone,
                                                          TransactionType transactionType, int quantity) {
        return StockTransaction.builder()
                .medicinePackageUnit(packageUnit)
                .zone(zone)
                .transactionType(transactionType)
                .quantity(quantity)
                .processedBy(UUID.randomUUID())
                .build();
    }

    public static Invitation createInvitation(Organization organization, String email) {
        return Invitation.create(organization, email);
    }
}
