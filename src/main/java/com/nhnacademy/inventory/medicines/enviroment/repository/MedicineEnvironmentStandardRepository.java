package com.nhnacademy.inventory.medicines.enviroment.repository;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicineEnvironmentStandardRepository extends JpaRepository<MedicineEnvironmentStandard, Long>
        , MedicineEnvironmentStandardRepositoryCustom {





}
