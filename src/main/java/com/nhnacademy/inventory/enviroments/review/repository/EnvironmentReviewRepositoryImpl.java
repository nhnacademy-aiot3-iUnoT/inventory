package com.nhnacademy.inventory.enviroments.review.repository;

import com.nhnacademy.inventory.enviroments.review.domain.EnvironmentReview;
import com.nhnacademy.inventory.enviroments.review.dto.QReviewHistorySummaryResponse;
import com.nhnacademy.inventory.enviroments.review.dto.QUnderReviewInventoryResponse;
import com.nhnacademy.inventory.enviroments.review.dto.ReviewHistorySummaryResponse;
import com.nhnacademy.inventory.enviroments.review.dto.UnderReviewInventoryResponse;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.nhnacademy.inventory.enviroments.event.domain.QEnvironmentEvent.environmentEvent;
import static com.nhnacademy.inventory.enviroments.review.domain.QEnvironmentReview.environmentReview;
import static com.nhnacademy.inventory.inventories.inventory.domain.QMedicineInventory.medicineInventory;
import static com.nhnacademy.inventory.medicines.medicine.domain.QMedicine.medicine;
import static com.nhnacademy.inventory.medicines.medicine.domain.QMedicinePackageUnit.medicinePackageUnit;
import static com.nhnacademy.inventory.organizations.organization.domain.QOrganization.organization;
import static com.nhnacademy.inventory.organizations.storage.domain.QStorage.storage;
import static com.nhnacademy.inventory.organizations.zone.domain.QZone.zone;

@Repository
@RequiredArgsConstructor
public class EnvironmentReviewRepositoryImpl implements EnvironmentReviewRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UnderReviewInventoryResponse> getUnderReviewInventories(List<Long> storageIds, Pageable pageable) {
        List<UnderReviewInventoryResponse> content = queryFactory
                .select(new QUnderReviewInventoryResponse(
                        medicineInventory.id,
                        organization.id,
                        storage.id,
                        zone.id,
                        medicine.id,
                        medicinePackageUnit.id,
                        organization.name,
                        storage.name,
                        zone.name,
                        medicine.productName,
                        medicinePackageUnit.packUnit,
                        medicineInventory.lotNumber,
                        medicineInventory.expirationDate,
                        medicineInventory.currentQuantity,
                        medicineInventory.createdAt,
                        medicineInventory.updatedAt,
                        medicineInventory.lastReviewAt
                ))
                .from(medicineInventory)
                .join(medicineInventory.zone, zone)
                .join(zone.storage, storage)
                .join(storage.organization, organization)
                .join(medicineInventory.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .where(
                        inventoryStorageIdEq(storageIds),
                        storage.status.eq(StorageStatus.ACTIVE),
                        medicineInventory.managementStatus.eq(ManagementStatus.UNDER_REVIEW)
                )
                .orderBy(getUnderReviewPageOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(medicineInventory.count())
                .from(medicineInventory)
                .where(
                        inventoryStorageIdEq(storageIds),
                        storage.status.eq(StorageStatus.ACTIVE),
                        medicineInventory.managementStatus.eq(ManagementStatus.UNDER_REVIEW)
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public Page<ReviewHistorySummaryResponse> getReviewHistories(List<Long> storageIds, Pageable pageable) {
        List<ReviewHistorySummaryResponse> content = queryFactory
                .select(new QReviewHistorySummaryResponse(
                        environmentEvent.id,
                        medicineInventory.id,
                        medicine.id,
                        medicinePackageUnit.id,
                        medicine.productName,
                        medicinePackageUnit.packUnit,
                        environmentReview.createdAt,
                        environmentReview.reviewerId,
                        environmentReview.isOut
                ))
                .from(environmentReview)
                .join(environmentReview.medicineInventory, medicineInventory)
                .join(medicineInventory.zone, zone)
                .join(medicineInventory.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .where(
                        reviewStorageIdEq(storageIds),
                        storage.status.eq(StorageStatus.ACTIVE)
                )
                .orderBy(getReviewHistoryPageOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(environmentReview.count())
                .from(environmentReview)
                .where(
                        reviewStorageIdEq(storageIds),
                        storage.status.eq(StorageStatus.ACTIVE)
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public Optional<EnvironmentReview> findByIdWithFetch(Long id) {
        EnvironmentReview review = queryFactory
                .selectFrom(environmentReview)
                .join(environmentReview.medicineInventory, medicineInventory).fetchJoin()
                .join(medicineInventory.zone, zone).fetchJoin()
                .join(zone.storage, storage).fetchJoin()
                .join(storage.organization, organization).fetchJoin()
                .join(medicineInventory.medicinePackageUnit, medicinePackageUnit).fetchJoin()
                .join(medicinePackageUnit.medicine, medicine).fetchJoin()
                .where(environmentReview.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(review);
    }

    private BooleanExpression inventoryStorageIdEq(List<Long> storageIds){
        if(storageIds == null || storageIds.isEmpty()){
            return Expressions.asBoolean(false).isTrue();
        }{
            return medicineInventory.zone.storage.id.in(storageIds);
        }
    }

    private BooleanExpression reviewStorageIdEq(List<Long> storageIds){
        if(storageIds == null || storageIds.isEmpty()){
            return Expressions.asBoolean(false).isTrue();
        }{
            return environmentReview.medicineInventory.zone.storage.id.in(storageIds);
        }
    }

    private OrderSpecifier<?> getUnderReviewPageOrderSpecifier(Pageable pageable){
        if(!pageable.getSort().isSorted()){
            return medicineInventory.createdAt.asc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        return order.isAscending() ? medicineInventory.createdAt.asc() : medicineInventory.createdAt.desc();
    }

    private OrderSpecifier<?> getReviewHistoryPageOrderSpecifier(Pageable pageable){
        if(!pageable.getSort().isSorted()){
            return environmentReview.createdAt.desc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        return order.isAscending() ? environmentReview.createdAt.asc() : environmentReview.createdAt.desc();
    }
}
