package com.nhnacademy.inventory.medicines.enviroment.repository;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MedicineEnvironmentStandardRepository extends JpaRepository<MedicineEnvironmentStandard, Long> {



    //조회용
    Optional<MedicineEnvironmentStandard> findByOrganizationIdAndMedicinePackageUnitId(Long organizationId, Long medicinePackageUnitId);
    Optional<MedicineEnvironmentStandard> findByMedicinePackageUnitId(Long packageUnitId);

    // 수정용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select ms
        from MedicineEnvironmentStandard ms
        where ms.organization.id = :organizationId
                and ms.medicinePackageUnit.id = :medicinePackageUnitId
        """
    )
    Optional<MedicineEnvironmentStandard> findByOrganizationIdAndPackageUnitIdForUpdate(Long organizationId, Long medicinePackageUnitId);


    void deleteByMedicinePackageUnitId(Long packageUnitId);




}
