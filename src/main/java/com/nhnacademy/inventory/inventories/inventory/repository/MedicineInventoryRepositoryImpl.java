package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.expiration.dto.QExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;


import com.nhnacademy.inventory.inventories.inventory.domain.QMedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;

import com.nhnacademy.inventory.inventories.inventory.dto.InventoryResponse;


import com.nhnacademy.inventory.inventories.inventory.dto.QInventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.QInventoryResponse;
import com.nhnacademy.inventory.medicines.medicine.domain.QMedicine;
import com.nhnacademy.inventory.medicines.medicine.domain.QMedicinePackageUnit;
import com.nhnacademy.inventory.organizations.department.domain.QStorageDepartment;
import com.nhnacademy.inventory.organizations.organization.domain.QOrganization;
import com.nhnacademy.inventory.organizations.storage.domain.QStorage;
import com.nhnacademy.inventory.organizations.zone.domain.QZone;
import com.querydsl.core.types.dsl.BooleanExpression;

import com.querydsl.jpa.JPAExpressions;
import com.nhnacademy.inventory.inventories.expiration.domain.ExpiringSearchFilterType;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventorySearchRequest;


import com.querydsl.core.types.OrderSpecifier;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private static final QOrganization organization = QOrganization.organization; // 조직 테이블
    private static final QMedicinePackageUnit packageUnit = QMedicinePackageUnit.medicinePackageUnit;


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
    public List<MedicineInventory> findOutboundInventories(
            Long medicinePackageUnitId,
            Long zoneId
    ) {
        return queryFactory
                .selectFrom(inventory)
                .where(
                        inventory.medicinePackageUnit.id.eq(medicinePackageUnitId),
                        inventory.zone.id.eq(zoneId),
                        inventory.managementStatus.eq(ManagementStatus.NORMAL),
                        inventory.currentQuantity.gt(0)
                )
                .orderBy(
                        inventory.expirationDate.asc(),
                        inventory.createdAt.asc()
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();

    }

    @Override
    public Optional<MedicineInventory> findByIdForUpdate(Long inventoryId) {

        MedicineInventory content = queryFactory
                .selectFrom(inventory)
                .where(inventory.id.eq(inventoryId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
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
                .join(inventory.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .join(inventory.zone, zone)
                .join(zone.storage, storage)
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
                .select(
                        storage.id,
                        medicinePackageUnit.id
                )
                .from(inventory)
                .join(inventory.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .join(inventory.zone, zone)
                .join(zone.storage, storage)
                .where(
                        JPAExpressions
                                .selectOne()
                                .from(storageDepartment)
                                .where(
                                        storageDepartment.storage.id.eq(storage.id),
                                        storageDepartment.department.id.in(departmentIds)
                                )
                                .exists(),

                        inventory.managementStatus.in(
                                ManagementStatus.NORMAL,
                                ManagementStatus.UNDER_REVIEW
                        ),

                        searchCondition,
                        storageCondition
                )
                .groupBy(
                        storage.id,
                        medicinePackageUnit.id
                )
                .fetch()
                .size();



        return new PageImpl<>(content,pageable,total);


    }

    @Override
    public Page<ExpiringInventoryResponse> findExpiringInventories(
            Long organizationId, ExpiringInventorySearchRequest request, Pageable pageable
    ) {
        LocalDate today = LocalDate.now();

        List<ExpiringInventoryResponse> content = queryFactory
                .select(new QExpiringInventoryResponse(
                        inventory.id,
                        medicine.id,
                        packageUnit.id,
                        organization.id,
                        storage.id,
                        zone.id,
                        medicine.productName,
                        packageUnit.packUnit,
                        organization.name,
                        storage.name,
                        zone.name,
                        inventory.lotNumber,
                        inventory.expirationDate,
                        inventory.currentQuantity
                ))
                .from(inventory)
                .join(inventory.zone, zone)
                .join(zone.storage, storage)
                .join(storage.organization, organization)
                .join(inventory.medicinePackageUnit, packageUnit)
                .join(packageUnit.medicine, medicine)
                .where(
                        organization.id.eq(organizationId),
                        storageIdEq(request.storageId()),
                        filterTypeEq(request.getFilterType(), today),
                        activeStatusEq()
                ).orderBy(getOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Long total = queryFactory
                .select(inventory.count())
                .from(inventory)
                .join(inventory.zone, zone)
                .join(zone.storage, storage)
                .join(storage.organization, organization)
                .where(
                        organization.id.eq(organizationId),
                        storageIdEq(request.storageId()),
                        filterTypeEq(request.getFilterType(), today),
                        activeStatusEq()
                )
                .fetchOne();


        return new PageImpl<>(
                content,
                pageable,
                total == null ? 0L : total
        );

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
                        zone.storage.id.in(storageIds),
                        inventory.managementStatus.notIn(
                                ManagementStatus.DISPOSAL,
                                ManagementStatus.DEPLETED
                        )

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
                        zone.storage.id.in(storageIds),
                        inventory.managementStatus.notIn(
                                ManagementStatus.DISPOSAL,
                                ManagementStatus.DEPLETED
                        )

                ).groupBy(storage.id,medicinePackageUnit.id)
                        .fetch()
                        .size();


        return new PageImpl<>(contents,pageable,total);
    }


    // 상세 조회
    @Override
    public Page<InventoryResponse> findByZonesAndPackUnitId(List<Long> zoneIds, Long packUnitId,Pageable pageable) {

        List<InventoryResponse> content = queryFactory.select(new QInventoryResponse(
                inventory.id,
                medicinePackageUnit.id,
                storage.id,
                zone.id,
                medicine.productName,
                medicine.itemCode,
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
                .where(zone.id.in(zoneIds),medicinePackageUnit.id.eq(packUnitId),

                        inventory.managementStatus.notIn(
                                ManagementStatus.DISPOSAL,
                                ManagementStatus.DEPLETED
                        )
                )
                .orderBy(medicine.productName.asc(),inventory.expirationDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Long total = queryFactory.select(inventory.id.count())
                .from(inventory)
                .join(inventory.medicinePackageUnit, medicinePackageUnit)
                .join(inventory.medicinePackageUnit.medicine, medicine)
                .join(inventory.zone, zone)
                .join(zone.storage, storage)
                .where(zone.id.in(zoneIds), medicinePackageUnit.id.eq(packUnitId),
                        inventory.managementStatus.notIn(
                                        ManagementStatus.DISPOSAL,
                                        ManagementStatus.DEPLETED
                                ))
                .fetchOne();



        return new PageImpl<>(content,pageable,total == null ? 0L : total);

    }


    /**
     * 대시보드용 임박 목록. 부서의 담당 저장소 여러 개를 한 번에 조회한다.
     */
    @Override
    public Page<ExpiringInventoryResponse> findExpiringInventoriesByStorageIds(
            Long organizationId, List<Long> storageIds, int withinDays, Pageable pageable
    ) {
        if (storageIds == null || storageIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        LocalDate today = LocalDate.now();

        List<ExpiringInventoryResponse> content = queryFactory
                .select(new QExpiringInventoryResponse(
                        inventory.id,
                        medicine.id,
                        packageUnit.id,
                        organization.id,
                        storage.id,
                        zone.id,
                        medicine.productName,
                        packageUnit.packUnit,
                        organization.name,
                        storage.name,
                        zone.name,
                        inventory.lotNumber,
                        inventory.expirationDate,
                        inventory.currentQuantity
                ))
                .from(inventory)
                .join(inventory.zone, zone)
                .join(zone.storage, storage)
                .join(storage.organization, organization)
                .join(inventory.medicinePackageUnit, packageUnit)
                .join(packageUnit.medicine, medicine)
                .where(
                        organization.id.eq(organizationId),
                        storage.id.in(storageIds),
                        expiringWithin(today, withinDays),
                        activeStatusEq()
                )
                .orderBy(inventory.expirationDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countExpiringWithinDays(organizationId, storageIds, withinDays);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 대시보드 KPI용 임박 품목 수. withinDays 이내로 유통기한이 남은 재고를 센다.
     */
    @Override
    public long countExpiringWithinDays(Long organizationId, List<Long> storageIds, int withinDays) {
        if (storageIds == null || storageIds.isEmpty()) {
            return 0L;
        }

        LocalDate today = LocalDate.now();

        Long count = queryFactory
                .select(inventory.count())
                .from(inventory)
                .join(inventory.zone, zone)
                .join(zone.storage, storage)
                .join(storage.organization, organization)
                .where(
                        organization.id.eq(organizationId),
                        storage.id.in(storageIds),
                        expiringWithin(today, withinDays),
                        activeStatusEq()
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    /** 이미 지난 것과 withinDays 이내로 남은 것을 함께 임박으로 본다. */
    private BooleanExpression expiringWithin(LocalDate today, int withinDays) {
        return inventory.expirationDate.loe(today.plusDays(withinDays));
    }

    private BooleanExpression storageIdEq(Long storageId) {
        return storageId != null ? storage.id.eq(storageId) : null;
    }

    private BooleanExpression filterTypeEq(ExpiringSearchFilterType filterType, LocalDate today) {
        if (filterType == ExpiringSearchFilterType.EXPIRED) {
            return inventory.expirationDate.lt(today);
        } else if (filterType == ExpiringSearchFilterType.WARNING) {
            LocalDate warningLimit = today.plusDays(7);
            return inventory.expirationDate.goe(today).and(inventory.expirationDate.loe(warningLimit));
        }
        return null;
    }

    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable){
        if(!pageable.getSort().isSorted()){
            return inventory.expirationDate.asc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        return order.isAscending() ? inventory.expirationDate.asc() : inventory.expirationDate.desc();
    }

    private BooleanExpression activeStatusEq() {
        return inventory.managementStatus.in(
                ManagementStatus.NORMAL,
                ManagementStatus.UNDER_REVIEW
        );
    }
}
