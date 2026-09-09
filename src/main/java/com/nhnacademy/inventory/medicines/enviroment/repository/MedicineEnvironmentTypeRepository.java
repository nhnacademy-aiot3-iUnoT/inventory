package com.nhnacademy.inventory.medicines.enviroment.repository;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedicineEnvironmentTypeRepository extends JpaRepository<MedicineEnvironmentType, Long> {


    @Query("""
            select t
            from MedicineEnvironmentType t
            where t.medicineEnvironmentStandard.id = :environmentStandardId

            """)
    List<MedicineEnvironmentType> findAllByMedicineEnvironmentStandardId(Long environmentStandardId);
    void deleteAllByMedicineEnvironmentStandardId(Long environmentStandardId);


    Optional<MedicineEnvironmentType> findByMedicineEnvironmentStandardAndEnvironmentType(MedicineEnvironmentStandard medicineEnvironmentStandard, EnvironmentType environmentType);


    void deleteByMedicineEnvironmentStandardAndEnvironmentType(MedicineEnvironmentStandard medicineEnvironmentStandard, EnvironmentType environmentType);
}
