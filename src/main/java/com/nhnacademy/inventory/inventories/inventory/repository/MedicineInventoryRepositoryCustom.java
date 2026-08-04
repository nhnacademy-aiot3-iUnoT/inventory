package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;

import java.time.LocalDate;
import java.util.Optional;

public interface MedicineInventoryRepositoryCustom {

    Optional<MedicineInventory> findByMedicinePackageUnitIdAndZoneIdAndLotNumberAndExpirationDate
            (Long medicinePackageUnitId, Long zoneId, String lotNumber, LocalDate expiration);


}
