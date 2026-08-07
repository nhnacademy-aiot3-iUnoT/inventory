package com.nhnacademy.inventory.medicines.enviroment.repository;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;

import java.util.Optional;

public interface MedicineEnvironmentStandardRepositoryCustom {

    Optional<MedicineEnvironmentStandard> findByOrganizationIdAndPackageUnitIdForUpdate(Long organizationId, Long medicinePackageUnitId);
    Optional<MedicineEnvironmentStandard> findByOrganizationIdAndPackageUnitId(Long organizationId, Long medicinePackageUnitId);


}
