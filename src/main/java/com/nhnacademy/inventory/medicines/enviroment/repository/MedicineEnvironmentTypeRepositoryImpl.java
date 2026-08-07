package com.nhnacademy.inventory.medicines.enviroment.repository;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.domain.QMedicineEnvironmentType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class MedicineEnvironmentTypeRepositoryImpl implements MedicineEnvironmentTypeRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private static final QMedicineEnvironmentType environmentType = QMedicineEnvironmentType.medicineEnvironmentType;



    @Override
    public List<MedicineEnvironmentType> findAllByMedicineEnvironmentStandardId(Long environmentStandardId) {

        return queryFactory.selectFrom(environmentType)
                .where(environmentType.medicineEnvironmentStandard.id.eq(environmentStandardId))
                .fetch();
    }




}
