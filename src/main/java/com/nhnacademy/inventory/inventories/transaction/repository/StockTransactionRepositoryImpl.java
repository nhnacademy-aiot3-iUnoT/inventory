package com.nhnacademy.inventory.inventories.transaction.repository;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.QStockTransactionSearchResponse;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchCondition;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

import static com.nhnacademy.inventory.inventories.transaction.domain.QStockTransaction.stockTransaction;
import static com.nhnacademy.inventory.medicines.medicine.domain.QMedicine.medicine;
import static com.nhnacademy.inventory.medicines.medicine.domain.QMedicinePackageUnit.medicinePackageUnit;

@RequiredArgsConstructor
public class StockTransactionRepositoryImpl implements StockTransactionRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StockTransactionSearchResponse> searchByCondition(Long zoneId, StockTransactionSearchCondition condition, Pageable pageable) {

        List<StockTransactionSearchResponse> responseList = queryFactory
                .select(new QStockTransactionSearchResponse(
                        stockTransaction.id,
                        medicine.productName,
                        medicinePackageUnit.packUnit,
                        stockTransaction.transactionType,
                        stockTransaction.quantity,
                        stockTransaction.reason,
                        stockTransaction.memo,
                        stockTransaction.processedBy,
                        stockTransaction.processedAt))
                .from(stockTransaction)
                .join(stockTransaction.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .where(
                        stockTransaction.zone.id.eq(zoneId),
                        medicineNameContains(condition.medicineName()),
                        transactionTypeEq(condition.transactionType()),
                        processedAtGoe(condition.startDate()),
                        processedAtLoe(condition.endDate())
                )
                .orderBy(stockTransaction.processedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(stockTransaction.count())
                .from(stockTransaction)
                .join(stockTransaction.medicinePackageUnit, medicinePackageUnit)
                .join(medicinePackageUnit.medicine, medicine)
                .where(
                        stockTransaction.zone.id.eq(zoneId),
                        medicineNameContains(condition.medicineName()),
                        transactionTypeEq(condition.transactionType()),
                        processedAtGoe(condition.startDate()),
                        processedAtLoe(condition.endDate())
                )
                .fetchOne();

        return new PageImpl<>(responseList, pageable, total != null ? total: 0L);
    }

    private BooleanExpression medicineNameContains(String medicineName){
        return StringUtils.hasText(medicineName) ? medicine.productName.contains(medicineName) : null;
    }

    private BooleanExpression transactionTypeEq(TransactionType transactionType){
        return transactionType != null ? stockTransaction.transactionType.eq(transactionType) : null;
    }

    private BooleanExpression processedAtGoe(LocalDate startDate){
        return startDate != null ? stockTransaction.processedAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression processedAtLoe(LocalDate endDate){
        return endDate != null ? stockTransaction.processedAt.lt(endDate.plusDays(1).atStartOfDay()) : null;
    }
}
