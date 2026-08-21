package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicineInventoryRepositoryCustom {

    Optional<MedicineInventory> findByMedicinePackageUnitIdAndZoneIdAndLotNumberAndExpirationDate
            (Long medicinePackageUnitId, Long zoneId, String lotNumber, LocalDate expiration);


    Page<InventoriesResponse> findAllInventoriesByDepartmentIds(String search,Long storageId, List<Long> departmentId, Pageable pageable);


    Page<InventoriesResponse> findAllInventories(String search, Long storageId, List<Long> storageIds, Pageable pageable);


}
