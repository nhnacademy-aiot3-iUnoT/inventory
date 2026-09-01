package com.nhnacademy.inventory.assistant.repository;

import com.nhnacademy.inventory.assistant.dto.EarlierExpiryLot;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EarlierExpiryStockRepository extends Repository<MedicineInventory, Long> {

    @Query("""
            SELECT new com.nhnacademy.inventory.assistant.dto.EarlierExpiryLot(
                       i.expirationDate, i.lotNumber, i.currentQuantity)
            FROM MedicineInventory i
            WHERE i.zone.id = :zoneId
              AND i.medicinePackageUnit.id = :medicinePackageUnitId
              AND i.expirationDate < :expirationDate
              AND i.currentQuantity > 0
              AND i.managementStatus = com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus.NORMAL
            ORDER BY i.expirationDate ASC
            """)
    List<EarlierExpiryLot> findEarlierExpiryLots(
            @Param("zoneId") Long zoneId,
            @Param("medicinePackageUnitId") Long medicinePackageUnitId,
            @Param("expirationDate") LocalDate expirationDate);
}
