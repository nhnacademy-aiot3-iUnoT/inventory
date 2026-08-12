package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long>, MedicineInventoryRepositoryCustom{




}
