package com.nhnacademy.inventory.chatbot.repository.impl;

import com.nhnacademy.inventory.chatbot.dto.ExpiringInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.LowStockInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.MedicineInventorySearchRow;
import com.nhnacademy.inventory.chatbot.dto.QExpiringInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.QLowStockInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.QMedicineInventorySearchRow;
import com.nhnacademy.inventory.chatbot.repository.MedicineInventoryChatbotRepository;
import com.nhnacademy.inventory.chatbot.dto.query.FindExpiringInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindLowStockInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.SearchMedicineInventoryQuery;
import static com.nhnacademy.inventory.inventories.inventory.domain.QMedicineInventory.medicineInventory;
import static com.nhnacademy.inventory.inventories.threshold.domain.QStockThreshold.stockThreshold;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
                .join(medicineInventory.medicinePackageUnit.medicine)
                .join(medicineInventory.zone.storage)
                // 저장소 접근 권한, 의약품명 포함, 실제 재고 있는 것만
                .where(medicineInventory.zone.storage.id.in(storageIds),
                        medicineInventory.medicinePackageUnit.medicine.productName.containsIgnoreCase(query.keyword()),
                        medicineInventory.currentQuantity.gt(0),
                        storageNameCondition(query.storageName()),
                        zoneNameCondition(query.zoneName()))
                .orderBy(
                        // 제품명 -> 포장단위 -> 유통기한
                        medicineInventory.medicinePackageUnit.medicine.productName.asc(),
                        medicineInventory.medicinePackageUnit.packUnit.asc(),
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
                .join(medicineInventory.medicinePackageUnit.medicine)
                .join(medicineInventory.zone.storage)
                .where(
                        medicineInventory.zone.storage.id.in(storageIds),
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
                .join(medicineInventory.medicinePackageUnit.medicine)
                .join(medicineInventory.zone.storage)
                .join(stockThreshold)
                .on(stockThreshold.medicinePackageUnit.eq(medicineInventory.medicinePackageUnit)
                        .and(stockThreshold.storage.eq(medicineInventory.zone.storage))
                        .and(stockThreshold.isActive.isTrue()))
                .where(
                        medicineInventory.zone.storage.id.in(storageIds),
                        lowStock,
                        storageNameCondition(query.storageName()),
                        zoneNameCondition(query.zoneName()))
                .orderBy(
                        medicineInventory.currentQuantity.asc(),
                        medicineInventory.medicinePackageUnit.medicine.productName.asc()
                )
                .limit(query.limit())
                .fetch();
    }

    private QMedicineInventorySearchRow searchRowProjection() {
        return new QMedicineInventorySearchRow(
                medicineInventory.medicinePackageUnit.id,
                medicineInventory.medicinePackageUnit.medicine.productName,
                medicineInventory.medicinePackageUnit.packUnit,
                medicineInventory.zone.storage.name,
                medicineInventory.zone.name,
                medicineInventory.currentQuantity
        );
    }

    private QExpiringInventoryRow expiringRowProjection() {
        return new QExpiringInventoryRow(
                medicineInventory.medicinePackageUnit.medicine.productName,
                medicineInventory.medicinePackageUnit.packUnit,
                medicineInventory.lotNumber,
                medicineInventory.expirationDate,
                medicineInventory.currentQuantity,
                medicineInventory.zone.storage.name,
                medicineInventory.zone.name
        );
    }

    private QLowStockInventoryRow lowStockRowProjection() {
        return new QLowStockInventoryRow(
                medicineInventory.medicinePackageUnit.medicine.productName,
                medicineInventory.medicinePackageUnit.packUnit,
                medicineInventory.currentQuantity,
                stockThreshold.threshold,
                medicineInventory.zone.storage.name,
                medicineInventory.zone.name
        );
    }

    private BooleanExpression medicineNameCondition(String name) {
        return name == null || name.isBlank()
                ? null
                : medicineInventory.medicinePackageUnit.medicine.productName.containsIgnoreCase(name.trim());
    }

    private BooleanExpression storageNameCondition(String name) {
        return name == null || name.isBlank() ? null : medicineInventory.zone.storage.name.containsIgnoreCase(name.trim());
    }

    private BooleanExpression zoneNameCondition(String name) {
        return name == null || name.isBlank() ? null : medicineInventory.zone.name.containsIgnoreCase(name.trim());
    }
}
