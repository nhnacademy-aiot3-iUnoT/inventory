package com.nhnacademy.inventory.medicines.enviroment.repository;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface MedicineEnvironmentTypeRepository extends JpaRepository<MedicineEnvironmentType, Long> {


    @Query("""
            select t
            from MedicineEnvironmentType t
            where t.medicineEnvironmentStandard.id = :environmentStandardId

            """)
    List<MedicineEnvironmentType> findAllByMedicineEnvironmentStandardId(Long environmentStandardId);
    void deleteAllByMedicineEnvironmentStandardId(Long environmentStandardId);




}
