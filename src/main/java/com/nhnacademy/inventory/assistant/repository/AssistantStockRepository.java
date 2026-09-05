package com.nhnacademy.inventory.assistant.repository;

import com.nhnacademy.inventory.assistant.dto.StockLot;
import com.nhnacademy.inventory.assistant.dto.ZoneStock;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssistantStockRepository extends Repository<MedicineInventory, Long> {

    @Query("""
            SELECT new com.nhnacademy.inventory.assistant.dto.StockLot(
                       i.expirationDate, i.lotNumber, i.currentQuantity)
            FROM MedicineInventory i
            WHERE i.zone.id = :zoneId
              AND i.medicinePackageUnit.id = :medicinePackageUnitId
              AND i.expirationDate < :expirationDate
              AND i.currentQuantity > 0
              AND i.managementStatus = com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus.NORMAL
            ORDER BY i.expirationDate ASC
            """)
    List<StockLot> findEarlierExpiryLots(
            @Param("zoneId") Long zoneId,
            @Param("medicinePackageUnitId") Long medicinePackageUnitId,
            @Param("expirationDate") LocalDate expirationDate);

    @Query("""
            SELECT new com.nhnacademy.inventory.assistant.dto.StockLot(
                       i.expirationDate, i.lotNumber, i.currentQuantity)
            FROM MedicineInventory i
            WHERE i.zone.id = :zoneId
              AND i.medicinePackageUnit.id = :medicinePackageUnitId
              AND i.currentQuantity > 0
              AND i.managementStatus = com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus.NORMAL
            ORDER BY i.expirationDate ASC
            """)
    List<StockLot> findRemainingLots(
            @Param("zoneId") Long zoneId,
            @Param("medicinePackageUnitId") Long medicinePackageUnitId);

    @Query("""
            SELECT new com.nhnacademy.inventory.assistant.dto.ZoneStock(
                       z.id, z.name, SUM(i.currentQuantity), MIN(i.expirationDate))
            FROM MedicineInventory i
            JOIN i.zone z
            WHERE z.storage.id = :storageId
              AND z.id <> :excludedZoneId
              AND i.medicinePackageUnit.id = :medicinePackageUnitId
              AND i.currentQuantity > 0
              AND i.managementStatus = com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus.NORMAL
            GROUP BY z.id, z.name
            ORDER BY MIN(i.expirationDate) ASC
            """)
    List<ZoneStock> findStockInOtherZones(
            @Param("storageId") Long storageId,
            @Param("excludedZoneId") Long excludedZoneId,
            @Param("medicinePackageUnitId") Long medicinePackageUnitId);

    @Query("""
            SELECT COALESCE(SUM(i.currentQuantity), 0)
            FROM MedicineInventory i
            WHERE i.zone.storage.id = :storageId
              AND i.medicinePackageUnit.id = :medicinePackageUnitId
              AND i.managementStatus = com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus.NORMAL
            """)
    long sumStorageQuantity(
            @Param("storageId") Long storageId,
            @Param("medicinePackageUnitId") Long medicinePackageUnitId);
}
