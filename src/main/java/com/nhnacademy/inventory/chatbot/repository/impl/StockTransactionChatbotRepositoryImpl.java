package com.nhnacademy.inventory.chatbot.repository.impl;

import com.nhnacademy.inventory.chatbot.dto.ConsumptionSummaryRow;
import com.nhnacademy.inventory.chatbot.dto.QConsumptionSummaryRow;
import com.nhnacademy.inventory.chatbot.dto.QStockPositionRow;
import com.nhnacademy.inventory.chatbot.dto.StockPositionRow;
import com.nhnacademy.inventory.chatbot.repository.StockTransactionChatbotRepository;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalReason;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.nhnacademy.inventory.inventories.inventory.domain.QMedicineInventory.medicineInventory;
import static com.nhnacademy.inventory.inventories.threshold.domain.QStockThreshold.stockThreshold;
import static com.nhnacademy.inventory.inventories.transaction.domain.QStockTransaction.stockTransaction;
import static com.nhnacademy.inventory.medicines.medicine.domain.QMedicine.medicine;
import static com.nhnacademy.inventory.medicines.medicine.domain.QMedicinePackageUnit.medicinePackageUnit;
import static com.nhnacademy.inventory.organizations.storage.domain.QStorage.storage;
import static com.nhnacademy.inventory.organizations.zone.domain.QZone.zone;

@Repository
@RequiredArgsConstructor
public class StockTransactionChatbotRepositoryImpl implements StockTransactionChatbotRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ConsumptionSummaryRow> sumOutboundTransactions(List<Long> storageIds, LocalDateTime start, LocalDateTime end) {
        if (storageIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(new QConsumptionSummaryRow(
                        stockTransaction.medicinePackageUnit.id,
                        storage.id,
                        stockTransaction.quantity.sum().longValue()
                ))
                .from(stockTransaction)
                .join(stockTransaction.zone, zone)
                .join(zone.storage, storage)
                .where(
                        storage.id.in(storageIds),
                        stockTransaction.transactionType.eq(TransactionType.OUTBOUND),
                        stockTransaction.processedAt.goe(start),
                        stockTransaction.processedAt.lt(end)
                )
                .groupBy(stockTransaction.medicinePackageUnit.id, storage.id)
                .fetch();
    }

    @Override
    public List<ConsumptionSummaryRow> sumDisposalTransactions(List<Long> storageIds, LocalDateTime start, LocalDateTime end) {
        if (storageIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(new QConsumptionSummaryRow(
                        stockTransaction.medicinePackageUnit.id,
                        storage.id,
                        stockTransaction.quantity.sum().longValue()
                ))
                .from(stockTransaction)
                .join(stockTransaction.zone, zone)
                .join(zone.storage, storage)
                .where(
                        storage.id.in(storageIds),
                        stockTransaction.transactionType.eq(TransactionType.DISPOSAL),
                        stockTransaction.reason.eq(DisposalReason.EXPIRED.name()),
                        stockTransaction.processedAt.goe(start),
                        stockTransaction.processedAt.lt(end)
                )
                .groupBy(stockTransaction.medicinePackageUnit.id, storage.id)
                .fetch();
    }

    @Override
    public List<StockPositionRow> findStockPositions(List<Long> storageIds, LocalDate expiryLimit) {

        if (storageIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(new QStockPositionRow(
                        medicinePackageUnit.id,
                        storage.id,
                        medicine.productName,
                        medicinePackageUnit.packUnit,
                        storage.name,
                        medicineInventory.currentQuantity.sum().longValue(),
                        new CaseBuilder()
                                .when(medicineInventory.expirationDate.loe(expiryLimit))
                                .then(medicineInventory.currentQuantity)
                                .otherwise(0)
                                .sum().longValue(),
                        stockThreshold.threshold
                ))
                .from(medicineInventory)
                .join(medicineInventory.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .join(medicineInventory.zone, zone)
                .join(zone.storage, storage)
                .leftJoin(stockThreshold)
                .on(stockThreshold.medicinePackageUnit.eq(medicineInventory.medicinePackageUnit)
                        .and(stockThreshold.storage.eq(storage))
                        .and(stockThreshold.isActive.isTrue()))
                .where(
                        storage.id.in(storageIds),
                        medicineInventory.managementStatus.eq(ManagementStatus.NORMAL)
                )
                .groupBy(
                        medicinePackageUnit.id,
                        storage.id,
                        medicine.productName,
                        medicinePackageUnit.packUnit,
                        storage.name,
                        stockThreshold.threshold
                )
                .fetch();
    }
}
