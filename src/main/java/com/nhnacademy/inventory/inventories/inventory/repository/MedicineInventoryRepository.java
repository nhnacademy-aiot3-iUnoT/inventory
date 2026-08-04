package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long>, MedicineInventoryRepositoryCustom{



}
