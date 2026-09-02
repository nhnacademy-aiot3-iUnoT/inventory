package com.nhnacademy.inventory.assistant.repository;

import com.nhnacademy.inventory.assistant.dto.StockLot;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/*
    재고 리포지토리를 건드리지 않으려고 비서 전용 조회 인터페이스를 따로 둔다.
    같은 엔티티에 리포지토리가 여럿이어도 문제되지 않는다.
 */
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
}
