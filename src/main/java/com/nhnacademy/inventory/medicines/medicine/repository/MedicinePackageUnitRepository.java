package com.nhnacademy.inventory.medicines.medicine.repository;


import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MedicinePackageUnitRepository extends JpaRepository<MedicinePackageUnit, Long>,MedicinePackageUnitRepositoryCustom {


    Long medicine(Medicine medicine);
}
