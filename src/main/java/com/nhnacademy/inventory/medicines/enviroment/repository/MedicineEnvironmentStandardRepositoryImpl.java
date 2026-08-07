package com.nhnacademy.inventory.medicines.enviroment.repository;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.QMedicineEnvironmentStandard;
import com.nhnacademy.inventory.medicines.enviroment.domain.QMedicineEnvironmentType;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MedicineEnvironmentStandardRepositoryImpl implements MedicineEnvironmentStandardRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QMedicineEnvironmentStandard medicineEnvironmentStandard = QMedicineEnvironmentStandard.medicineEnvironmentStandard;
    private static final QMedicineEnvironmentType medicineEnvironmentType = QMedicineEnvironmentType.medicineEnvironmentType;


    // 조회용

    @Override
    public Optional<MedicineEnvironmentStandard> findByOrganizationIdAndPackageUnitId(Long organizationId, Long medicinePackageUnitId) {

        MedicineEnvironmentStandard standard = queryFactory.select(medicineEnvironmentStandard)
                .from(medicineEnvironmentStandard)
                .distinct()
                .leftJoin(medicineEnvironmentType)
                .fetchJoin()
                .where(medicineEnvironmentStandard.medicinePackageUnit.id.eq(medicinePackageUnitId),
                        medicineEnvironmentStandard.organization.id.eq(organizationId))
                .fetchOne();


        return Optional.ofNullable(standard);


    }



    // 수정용
    @Override
    public Optional<MedicineEnvironmentStandard> findByOrganizationIdAndPackageUnitIdForUpdate(Long organizationId, Long medicinePackageUnitId) {

        MedicineEnvironmentStandard standard = queryFactory.select(medicineEnvironmentStandard)
                .from(medicineEnvironmentStandard)
                .distinct()
                .leftJoin(medicineEnvironmentType)
                .fetchJoin()
                .where(medicineEnvironmentStandard.medicinePackageUnit.id.eq(medicinePackageUnitId),
                        medicineEnvironmentStandard.organization.id.eq(organizationId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();


        return Optional.ofNullable(standard);
    }



}
