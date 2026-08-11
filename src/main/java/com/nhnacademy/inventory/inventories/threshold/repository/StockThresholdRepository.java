package com.nhnacademy.inventory.inventories.threshold.repository;

import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockThresholdRepository extends JpaRepository<StockThreshold, Long> {
    List<StockThreshold> findAllByStorage(Storage storage);

    Optional<StockThreshold> findByIdAndStorage(Long id, Storage storage);

    Optional<StockThreshold> findByStorageAndMedicinePackageUnit(Storage storage, MedicinePackageUnit medicinePackageUnit);
}
