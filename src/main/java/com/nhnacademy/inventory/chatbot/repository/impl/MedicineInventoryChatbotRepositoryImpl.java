package com.nhnacademy.inventory.chatbot.repository.impl;

import com.nhnacademy.inventory.chatbot.dto.ExpiringInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.LowStockInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.MedicineInventorySearchRow;
import com.nhnacademy.inventory.chatbot.dto.MedicinePackageUnitTargetRow;
import com.nhnacademy.inventory.chatbot.dto.QExpiringInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.QLowStockInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.QMedicineInventorySearchRow;
import com.nhnacademy.inventory.chatbot.dto.QMedicinePackageUnitTargetRow;
import com.nhnacademy.inventory.chatbot.dto.QZoneTargetRow;
import com.nhnacademy.inventory.chatbot.dto.ZoneTargetRow;
import com.nhnacademy.inventory.chatbot.dto.query.FindExpiringInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindLowStockInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindMedicinePackageUnitTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindZoneTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.query.SearchMedicineInventoryQuery;
import com.nhnacademy.inventory.chatbot.repository.MedicineInventoryChatbotRepository;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.nhnacademy.inventory.inventories.inventory.domain.QMedicineInventory.medicineInventory;
import static com.nhnacademy.inventory.inventories.threshold.domain.QStockThreshold.stockThreshold;
import static com.nhnacademy.inventory.medicines.medicine.domain.QMedicine.medicine;
import static com.nhnacademy.inventory.medicines.medicine.domain.QMedicinePackageUnit.medicinePackageUnit;
import static com.nhnacademy.inventory.organizations.storage.domain.QStorage.storage;
import static com.nhnacademy.inventory.organizations.zone.domain.QZone.zone;

@Repository
@RequiredArgsConstructor
public class MedicineInventoryChatbotRepositoryImpl implements MedicineInventoryChatbotRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<MedicineInventorySearchRow> search(SearchMedicineInventoryQuery query) {
        List<Long> storageIds = query.storageIds();

        if (storageIds.isEmpty()) {
            return List.of();
        }

        return queryFactory.select(searchRowProjection())
                .from(medicineInventory)
                .join(medicineInventory.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .join(medicineInventory.zone, zone)
                .join(zone.storage, storage)
                // 저장소 접근 권한, 의약품명 포함, 실제 재고 있는 것만
                .where(storage.id.in(storageIds),
                        medicine.productName.containsIgnoreCase(query.keyword()),
                        medicineInventory.currentQuantity.gt(0),
                        storageNameCondition(query.storageName()),
                        zoneNameCondition(query.zoneName()))
                .orderBy(
                        // 제품명 -> 포장단위 -> 유통기한
                        medicine.productName.asc(),
                        medicinePackageUnit.packUnit.asc(),
                        medicineInventory.expirationDate.asc())
                .limit(query.limit())
                .fetch();
    }

    @Override
    public List<ExpiringInventoryRow> findExpiring(FindExpiringInventoryQuery query) {
        List<Long> storageIds = query.storageIds();

        if (storageIds.isEmpty()) {
            return List.of();
        }

        return queryFactory.select(expiringRowProjection())
                .from(medicineInventory)
                .join(medicineInventory.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .join(medicineInventory.zone, zone)
                .join(zone.storage, storage)
                .where(
                        storage.id.in(storageIds),
                        medicineInventory.expirationDate.between(query.today(), query.limitDate()),
                        medicineInventory.currentQuantity.gt(0),
                        medicineNameCondition(query.medicineName()),
                        storageNameCondition(query.storageName()),
                        zoneNameCondition(query.zoneName()))
                .orderBy(medicineInventory.expirationDate.asc())
                .limit(query.limit())
                .fetch();
    }

    @Override
    public List<LowStockInventoryRow> findLowStock(FindLowStockInventoryQuery query) {
        List<Long> storageIds = query.storageIds();

        if (storageIds.isEmpty()) {
            return List.of();
        }

        // loe : <=
        BooleanExpression lowStock = medicineInventory.currentQuantity.loe(stockThreshold.threshold);

        return queryFactory.select(lowStockRowProjection())
                .from(medicineInventory)
                .join(medicineInventory.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .join(medicineInventory.zone, zone)
                .join(zone.storage, storage)
                .join(stockThreshold)
                .on(stockThreshold.medicinePackageUnit.eq(medicineInventory.medicinePackageUnit)
                        .and(stockThreshold.storage.eq(storage))
                        .and(stockThreshold.isActive.isTrue()))
                .where(
                        storage.id.in(storageIds),
                        lowStock,
                        storageNameCondition(query.storageName()),
                        zoneNameCondition(query.zoneName()))
                .orderBy(
                        medicineInventory.currentQuantity.asc(),
                        medicine.productName.asc()
                )
                .limit(query.limit())
                .fetch();
    }

    @Override
    public List<MedicinePackageUnitTargetRow> findPackageUnitTargets(
            FindMedicinePackageUnitTargetQuery query
    ) {
        return queryFactory.select(new QMedicinePackageUnitTargetRow(
                        medicinePackageUnit.id,
                        medicine.productName,
                        medicinePackageUnit.packUnit
                ))
                .from(medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .where(
                        medicine.productName.containsIgnoreCase(query.medicineName().trim()),
                        packUnitCondition(query.packUnit())
                )
                .orderBy(medicine.productName.asc(), medicinePackageUnit.packUnit.asc())
                .limit(query.limit())
                .fetch();
    }

    @Override
    public Optional<MedicinePackageUnitTargetRow> findPackageUnitTargetById(Long packageUnitId) {
        MedicinePackageUnitTargetRow target = queryFactory.select(new QMedicinePackageUnitTargetRow(
                        medicinePackageUnit.id,
                        medicine.productName,
                        medicinePackageUnit.packUnit
                ))
                .from(medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .where(medicinePackageUnit.id.eq(packageUnitId))
                .fetchOne();

        return Optional.ofNullable(target);
    }

    @Override
    public List<ZoneTargetRow> findZoneTargets(FindZoneTargetQuery query) {
        if (query.storageIds().isEmpty()) {
            return List.of();
        }

        return queryFactory.select(new QZoneTargetRow(
                        zone.id,
                        storage.name,
                        zone.name
                ))
                .from(zone)
                .join(zone.storage, storage)
                .where(
                        storage.id.in(query.storageIds()),
                        storage.status.eq(StorageStatus.ACTIVE),
                        zone.status.eq(ZoneStatus.ACTIVE),
                        storage.name.containsIgnoreCase(query.storageName().trim()),
                        zone.name.containsIgnoreCase(query.zoneName().trim())
                )
                .orderBy(storage.name.asc(), zone.name.asc())
                .limit(query.limit())
                .fetch();
    }

    @Override
    public Optional<ZoneTargetRow> findZoneTargetById(Long zoneId, List<Long> storageIds) {
        if (storageIds.isEmpty()) {
            return Optional.empty();
        }

        ZoneTargetRow target = queryFactory.select(new QZoneTargetRow(
                        zone.id,
                        storage.name,
                        zone.name
                ))
                .from(zone)
                .join(zone.storage, storage)
                .where(
                        zone.id.eq(zoneId),
                        storage.id.in(storageIds),
                        storage.status.eq(StorageStatus.ACTIVE),
                        zone.status.eq(ZoneStatus.ACTIVE)
                )
                .fetchOne();

        return Optional.ofNullable(target);
    }

    private QMedicineInventorySearchRow searchRowProjection() {
        return new QMedicineInventorySearchRow(
                medicinePackageUnit.id,
                medicine.productName,
                medicinePackageUnit.packUnit,
                storage.name,
                zone.name,
                medicineInventory.currentQuantity,
                medicineInventory.id,
                medicineInventory.lotNumber,
                medicineInventory.expirationDate,
                medicineInventory.managementStatus
        );
    }

    private QExpiringInventoryRow expiringRowProjection() {
        return new QExpiringInventoryRow(
                medicine.productName,
                medicinePackageUnit.packUnit,
                medicineInventory.lotNumber,
                medicineInventory.expirationDate,
                medicineInventory.currentQuantity,
                storage.name,
                zone.name
        );
    }

    private QLowStockInventoryRow lowStockRowProjection() {
        return new QLowStockInventoryRow(
                medicine.productName,
                medicinePackageUnit.packUnit,
                medicineInventory.currentQuantity,
                stockThreshold.threshold,
                storage.name,
                zone.name
        );
    }

    private BooleanExpression medicineNameCondition(String name) {
        return name == null || name.isBlank()
                ? null
                : medicine.productName.containsIgnoreCase(name.trim());
    }

    private BooleanExpression packUnitCondition(String packUnit) {
        return Arrays.stream(packUnit.trim().split("[\\s\\p{Punct}]+"))
                .filter(keyword -> !keyword.isBlank())
                .map(medicinePackageUnit.packUnit::containsIgnoreCase)
                .reduce(BooleanExpression::and)
                .orElse(null);
    }

    private BooleanExpression storageNameCondition(String name) {
        return name == null || name.isBlank() ? null : storage.name.containsIgnoreCase(name.trim());
    }

    private BooleanExpression zoneNameCondition(String name) {
        return name == null || name.isBlank() ? null : zone.name.containsIgnoreCase(name.trim());
    }
}
