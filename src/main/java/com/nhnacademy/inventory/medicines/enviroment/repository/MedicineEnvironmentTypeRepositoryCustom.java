package com.nhnacademy.inventory.medicines.enviroment.repository;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;

import java.util.List;


public interface MedicineEnvironmentTypeRepositoryCustom {

    List<MedicineEnvironmentType> findAllByMedicineEnvironmentStandardId(Long environmentStandardId);
    void deleteAllByMedicineEnvironmentStandardId(Long environmentStandardId);



}
