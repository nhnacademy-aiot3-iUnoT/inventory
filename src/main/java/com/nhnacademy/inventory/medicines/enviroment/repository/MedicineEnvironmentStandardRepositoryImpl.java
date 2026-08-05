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
    private final QMedicineEnvironmentStandard medicineEnvironmentStandard = QMedicineEnvironmentStandard.medicineEnvironmentStandard;
    private final QMedicineEnvironmentType medicineEnvironmentType = QMedicineEnvironmentType.medicineEnvironmentType;


    @Override
    public Optional<MedicineEnvironmentStandard> findWithEnvironmentTypes(Long organizationId, Long medicinePackageId) {

        MedicineEnvironmentStandard standard = queryFactory.select(medicineEnvironmentStandard)
                .from(medicineEnvironmentStandard)
                .distinct()
                .leftJoin(medicineEnvironmentType)
                .fetchJoin()
                .where(medicineEnvironmentStandard.medicinePackageUnit.id.eq(medicinePackageId),
                        medicineEnvironmentStandard.organization.id.eq(organizationId))
                .fetchOne();


        return Optional.ofNullable(standard);
    }

}
