package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.domain.QMedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;


import com.nhnacademy.inventory.inventories.inventory.dto.InventoryInfoResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.QInventoriesResponse;

import com.nhnacademy.inventory.inventories.inventory.dto.QInventoryInfoResponse;
import com.nhnacademy.inventory.medicines.medicine.domain.QMedicine;
import com.nhnacademy.inventory.medicines.medicine.domain.QMedicinePackageUnit;
import com.nhnacademy.inventory.organizations.department.domain.QStorageDepartment;
import com.nhnacademy.inventory.organizations.storage.domain.QStorage;
import com.nhnacademy.inventory.organizations.zone.domain.QZone;
import com.querydsl.core.types.dsl.BooleanExpression;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MedicineInventoryRepositoryImpl implements MedicineInventoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private static final QMedicineInventory inventory = QMedicineInventory.medicineInventory;
    private static final QMedicinePackageUnit medicinePackageUnit = QMedicinePackageUnit.medicinePackageUnit;
    private static final QMedicine medicine = QMedicine.medicine;
    private static final QZone zone = QZone.zone;
    private static final QStorage storage = QStorage.storage;
    private static final QStorageDepartment storageDepartment = QStorageDepartment.storageDepartment;


    @Override
    public Optional<MedicineInventory> findByMedicinePackageUnitIdAndZoneIdAndLotNumberAndExpirationDate
            (Long medicinePackageUnitId, Long zoneId, String lotNumber, LocalDate expiration) {

        MedicineInventory content = queryFactory.selectFrom(inventory)
                .where(
                        inventory.medicinePackageUnit.id.eq(medicinePackageUnitId),
                        inventory.zone.id.eq(zoneId),
                        inventory.lotNumber.eq(lotNumber),
                        inventory.expirationDate.eq(expiration)
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE) //비관적 락
                .fetchOne();


        return Optional.ofNullable(content);
    }


    @Override
    public Page<InventoriesResponse> findAllInventoriesByDepartmentIds(String search,Long storageId, List<Long> departmentIds, Pageable pageable) {


        // 제품명 또는 품목기준코드로 조회
        BooleanExpression searchCondition =
                search == null || search.isBlank()
                        ? null :
                        medicine.productName
                                .containsIgnoreCase(search.trim())
                                .or(medicine.itemCode.containsIgnoreCase(search.trim()));


        // 저장소 필터적용
        BooleanExpression storageCondition =
                storageId == null ?
                        null :
                        zone.storage.id.eq(storageId);



        List<InventoriesResponse> content = queryFactory.select(
                new QInventoriesResponse(
                        inventory.zone.storage.id,
                        inventory.medicinePackageUnit.id,
                        inventory.medicinePackageUnit.medicine.productName,
                        inventory.medicinePackageUnit.medicine.itemCode,
                        inventory.medicinePackageUnit.packUnit,
                        inventory.expirationDate.min(),
                        inventory.zone.storage.name,
                        inventory.currentQuantity.sum()
                ))
                .from(inventory)
                .join(inventory.medicinePackageUnit,medicinePackageUnit)
                .join(medicinePackageUnit.medicine,medicine)
                .join(inventory.zone,zone)
                .join(zone.storage,storage)
                .where(
                        JPAExpressions
                                .selectOne()
                                .from(storageDepartment)
                                .where(
                                       storageDepartment.storage.id.eq(storage.id),
                                       storageDepartment.department.id.in(departmentIds)
                                ).exists(),

                        inventory.managementStatus.in(
                                ManagementStatus.NORMAL,
                                ManagementStatus.LOW_STOCK,
                                ManagementStatus.NEAR_EXPIRATION,
                                ManagementStatus.UNDER_REVIEW
                        ),

                        searchCondition,
                        storageCondition

                        )
                .groupBy(storage.id,
                        medicinePackageUnit.id,
                        medicine.productName,
                        medicine.itemCode,
                        medicinePackageUnit.packUnit,
                        storage.name
                        )
                .orderBy(medicine.productName.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        long total = queryFactory
                .select(storage.id, medicinePackageUnit.id)
                .from(inventory)
                .join(inventory.medicinePackageUnit,medicinePackageUnit)
                .join(medicinePackageUnit.medicine,medicine)
                .join(inventory.zone,zone)
                .join(zone.storage,storage)
                .where(
                        JPAExpressions
                                .selectOne()
                                .from(storageDepartment)
                                .where(

                                     storageDepartment.department.id.in(departmentIds),
                                     storageDepartment.storage.id.eq(storage.id)
                                ).exists(),
                        inventory.managementStatus.in(
                                ManagementStatus.NORMAL,
                                ManagementStatus.LOW_STOCK,
                                ManagementStatus.NEAR_EXPIRATION,
                                ManagementStatus.UNDER_REVIEW

                                ),
                        searchCondition,
                        storageCondition

                ).groupBy(
                        storage.id,
                        medicinePackageUnit.id,
                        medicine.productName,
                        medicine.itemCode,
                        medicinePackageUnit.packUnit,
                        storage.name
                ).fetch().size();


        return new PageImpl<>(content,pageable,total);

    }

    @Override
    public Page<InventoriesResponse> findAllInventories(String search, Long storageId, List<Long> storageIds, Pageable pageable) {


        BooleanExpression searchCondition =
                search == null || search.isBlank() ? null :
                        medicine.productName.containsIgnoreCase(search.trim())
                                .or(medicine.itemCode.containsIgnoreCase(search.trim()));


        BooleanExpression storageCondition = storageId == null ? null :
                storage.id.eq(storageId);


        List<InventoriesResponse> contents = queryFactory.select(new QInventoriesResponse(
                inventory.zone.storage.id,
                inventory.medicinePackageUnit.id,
                inventory.medicinePackageUnit.medicine.productName,
                inventory.medicinePackageUnit.medicine.itemCode,
                inventory.medicinePackageUnit.packUnit,
                inventory.expirationDate.min(),
                inventory.zone.storage.name,
                inventory.currentQuantity.sum()

        )).from(inventory)
                .join(inventory.medicinePackageUnit,medicinePackageUnit)
                .join(inventory.medicinePackageUnit.medicine,medicine)
                .join(inventory.zone,zone)
                .join(zone.storage,storage)
                .where(
                    searchCondition,
                        storageCondition,
                        zone.storage.id.in(storageIds)
                )
                .groupBy(medicinePackageUnit.id,storage.id)
                .orderBy(medicine.productName.asc(),inventory.expirationDate.min().asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        long total = queryFactory.select(storage.id,medicinePackageUnit.id)
                .from(inventory)
                .join(inventory.medicinePackageUnit,medicinePackageUnit)
                .join(inventory.medicinePackageUnit.medicine,medicine)
                .join(inventory.zone,zone)
                .join(zone.storage,storage)
                .where(
                        searchCondition,
                        storageCondition,
                        zone.storage.id.in(storageIds)

                ).groupBy(storage.id,medicinePackageUnit.id)
                        .fetch()
                        .size();


        return new PageImpl<>(contents,pageable,total);
    }


    // Long medicinePackUnitId,
    //        Long storageId,
    //        Long zoneId,
    //        String productName,
    //        String lotNumber,
    //        LocalDate expirationDate,
    //        Long currentQuantity,
    //        String storageName,
    //        String zoneName,
    //        ManagementStatus managementStatus


    @Override
    public Page<InventoryInfoResponse> findByZonesAndPackUnitId(List<Long> zoneIds, Long packUnitId,Pageable pageable) {

        List<InventoryInfoResponse> content = queryFactory.select(new QInventoryInfoResponse(
                medicinePackageUnit.id,
                storage.id,
                zone.id,
                medicine.productName,
                inventory.lotNumber,
                inventory.expirationDate,
                inventory.currentQuantity,
                storage.name,
                zone.name,
                inventory.managementStatus
        )).from(inventory)
                .join(inventory.medicinePackageUnit,medicinePackageUnit)
                .join(inventory.medicinePackageUnit.medicine,medicine)
                .join(inventory.zone,zone)
                .join(zone.storage,storage)
                .where(zone.id.in(zoneIds),medicinePackageUnit.id.eq(packUnitId))
                .orderBy(medicine.productName.asc(),inventory.expirationDate.min().asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        long total = queryFactory.select()
                .from(inventory)
                .join(inventory.medicinePackageUnit,medicinePackageUnit)
                .join(inventory.medicinePackageUnit.medicine,medicine)
                .join(inventory.zone,zone)
                .join(zone.storage,storage)
                .where(zone.id.in(zoneIds),medicinePackageUnit.id.eq(packUnitId))
                .fetch().size();



        return new PageImpl<>(content,pageable,total);
    }
}
