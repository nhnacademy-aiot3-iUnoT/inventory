package com.nhnacademy.inventory.medicines.medicine.repository;

import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    boolean existsByItemCode(String itemCode);

    List<Medicine> findAllByProductNameContainingIgnoreCase(String productName);



}
