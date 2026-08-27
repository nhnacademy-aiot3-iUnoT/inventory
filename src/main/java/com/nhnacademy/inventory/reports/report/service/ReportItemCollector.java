package com.nhnacademy.inventory.reports.report.service;

import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.reports.report.domain.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportItemCollector {
    private final StockTransactionService stockTransactionService;

    public void collectReportItems(Report report) {
        List<StockTransaction> transactions = stockTransactionService.findTransactionsForStorageReport(
                report.getStorageId(),
                List.of(TransactionType.INBOUND, TransactionType.OUTBOUND, TransactionType.DISPOSAL),
                report.getPeriodStart(),
                report.getPeriodEnd());

        sumByUnit(transactions, TransactionType.INBOUND)
                .forEach(report::addInbound);

        sumByUnit(transactions, TransactionType.OUTBOUND)
                .forEach(report::addOutbound);

        sumByUnit(transactions, TransactionType.DISPOSAL)
                .forEach(report::addDisposal);
    }

    private Map<MedicinePackageUnit, Integer> sumByUnit(List<StockTransaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getTransactionType() == type)
                .collect(Collectors.groupingBy(StockTransaction::getMedicinePackageUnit, Collectors.summingInt(StockTransaction::getQuantity)));
    }
}
