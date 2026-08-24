package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.expiration.domain.ExpiringSearchFilterType;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventorySearchRequest;
import com.nhnacademy.inventory.inventories.expiration.dto.QExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.domain.QMedicineInventory;
import com.nhnacademy.inventory.medicines.medicine.domain.QMedicine;
import com.nhnacademy.inventory.medicines.medicine.domain.QMedicinePackageUnit;
import com.nhnacademy.inventory.organizations.organization.domain.QOrganization;
import com.nhnacademy.inventory.organizations.storage.domain.QStorage;
import com.nhnacademy.inventory.organizations.zone.domain.QZone;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
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

import static com.nhnacademy.inventory.organizations.storage.domain.QStorage.storage;

@Repository
@RequiredArgsConstructor
public class MedicineInventoryRepositoryImpl implements MedicineInventoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private static final QMedicineInventory inventory = QMedicineInventory.medicineInventory;
    private static final QZone zone = QZone.zone;
    private static final QStorage storage = QStorage.storage;
    private static final QOrganization organization = QOrganization.organization; // 조직 테이블
    private static final QMedicinePackageUnit packageUnit = QMedicinePackageUnit.medicinePackageUnit;
    private static final QMedicine medicine = QMedicine.medicine;

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
                        filterTypeEq(request.getFilterType(), today)
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
                        filterTypeEq(request.getFilterType(), today)
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, totalCount);
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
}
