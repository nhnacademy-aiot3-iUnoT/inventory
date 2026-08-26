package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventorySearchRequest;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;

import com.nhnacademy.inventory.inventories.inventory.dto.InventoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicineInventoryRepositoryCustom {

    Optional<MedicineInventory> findByMedicinePackageUnitIdAndZoneIdAndLotNumberAndExpirationDate
            (Long medicinePackageUnitId, Long zoneId, String lotNumber, LocalDate expiration);

    List<MedicineInventory> findOutboundInventories(
            Long medicinePackageUnitId,
            Long zoneId
    );

    Page<InventoriesResponse> findAllInventoriesByDepartmentIds(String search,Long storageId, List<Long> departmentId, Pageable pageable);


    Page<InventoriesResponse> findAllInventories(String search, Long storageId, List<Long> storageIds, Pageable pageable);


    Page<InventoryResponse> findByZonesAndPackUnitId(List<Long> zoneIds, Long packUnitId, Pageable pageable);



    Page<ExpiringInventoryResponse> findExpiringInventories(
            Long organizationId,
            ExpiringInventorySearchRequest request,
            Pageable pageable
    );

    Optional<MedicineInventory> findByIdForUpdate(Long inventoryId);
}
