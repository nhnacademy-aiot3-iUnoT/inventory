package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;


public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long>, MedicineInventoryRepositoryCustom{


    long countByZone_StorageAndExpirationDateBefore(Storage zoneStorage, LocalDate expirationDateBefore);

    long countByZone_StorageAndExpirationDateBetween(Storage zoneStorage, LocalDate expirationDateAfter, LocalDate expirationDateBefore);
}
