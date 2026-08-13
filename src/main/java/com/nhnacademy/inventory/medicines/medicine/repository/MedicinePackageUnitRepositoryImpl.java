package com.nhnacademy.inventory.medicines.medicine.repository;

import com.nhnacademy.inventory.medicines.medicine.domain.QMedicine;
import com.nhnacademy.inventory.medicines.medicine.domain.QMedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.QMedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.QMedicinePackageSearchResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MedicinePackageUnitRepositoryImpl implements MedicinePackageUnitRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private static final QMedicinePackageUnit packageUnit = QMedicinePackageUnit.medicinePackageUnit;
    private static final QMedicine medicine = QMedicine.medicine;


    // 제품명 조회
    @Override
    public Page<MedicinePackageSearchResponse> findAllWithMedicineByProductName(String productName, Pageable pageable) {

        List<MedicinePackageSearchResponse> content = queryFactory.select(new QMedicinePackageSearchResponse(
                medicine.id,
                packageUnit.id,
                medicine.itemCode,
                medicine.productName,
                medicine.companyName,
                packageUnit.packUnit

        )).from(packageUnit)
                .join(packageUnit.medicine,medicine)
                .where(medicine.productName.contains(productName))
                .orderBy(medicine.itemCode.asc(),medicine.productName.asc(),packageUnit.packUnit.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Long total = queryFactory.select(packageUnit.count())
                .from(packageUnit)
                .join(packageUnit.medicine,medicine)
                .where(medicine.productName.contains(productName))
                .fetchOne();




        //fetchOne 결과가 하나일때

        return new PageImpl<>(content,pageable,total == null ? 0L : total);
    }

    //품목기준코드 조회

    @Override
    public Page<MedicinePackageSearchResponse> findAllWithMedicineByItemCode(String itemCode, Pageable pageable) {

        List<MedicinePackageSearchResponse> content = queryFactory.select(new QMedicinePackageSearchResponse(
                        medicine.id,
                        packageUnit.id,
                        medicine.itemCode,
                        medicine.productName,
                        medicine.companyName,
                        packageUnit.packUnit
                ))
                .from(packageUnit)
                .join(packageUnit.medicine,medicine)
                .where(medicine.itemCode.eq(itemCode))
                .orderBy(medicine.itemCode.asc(),medicine.productName.asc(),packageUnit.packUnit.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(packageUnit.count())
                .from(packageUnit)
                .join(packageUnit.medicine,medicine)
                .where(medicine.itemCode.eq(itemCode))
                .fetchOne();


        return new PageImpl<>(content,pageable,total == null ? 0 : total);
    }


    @Override
    public Optional<MedicinePackageDetailResponse> findDetailMedicine(Long packUnitId) {

        MedicinePackageDetailResponse content = queryFactory.select(new QMedicinePackageDetailResponse(
                medicine.id,
                packageUnit.id,
                medicine.itemCode,
                medicine.productName,
                medicine.companyName,
                medicine.storageMethod,
                medicine.validityPeriod,
                packageUnit.packUnit,
                medicine.narcoticKindCode
        )).from(packageUnit)
                .join(packageUnit.medicine,medicine)
                .where(packageUnit.id.eq(packUnitId))
                .fetchOne();


        return Optional.ofNullable(content);
    }
}
