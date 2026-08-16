package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.dto.ReportCreatedEvent;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.OrganizationMemberValidator;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportCreateUseCase {

    private final ReportService reportService;
    private final OrganizationMemberValidator memberValidator;
    private final StockTransactionService stockTransactionService;
    private final ApplicationEventPublisher eventPublisher;

    // 주간 리포트를 생성한다. 이미 주간 리포트가 있으면 그걸 반환하고, 없으면 생성한다.
    @Transactional
    public ReportInfoResponse execute(Long organizationId, LocalDate periodStart) {
        memberValidator.validate(organizationId);

        Report report = reportService.find(organizationId, ReportType.WEEKLY, periodStart)
                .orElseGet(() -> generate(organizationId, periodStart));

        return ReportInfoResponse.of(report);
    }

    private Report generate(Long organizationId, LocalDate periodStart) {
        Report report = Report.weeklyOf(organizationId, periodStart);

        Report saved = reportService.register(report);

        collectReportItems(saved);

        eventPublisher.publishEvent(new ReportCreatedEvent(saved.getId()));

        return saved;
    }

    private void collectReportItems(Report report) {
        List<StockTransaction> transactions = stockTransactionService.findTransactionsForReport(
                report.getOrganizationId(),
                List.of(TransactionType.OUTBOUND, TransactionType.DISPOSAL),
                report.getPeriodStart(),
                report.getPeriodEnd());

        sumByUnit(transactions, TransactionType.OUTBOUND)
                .forEach(report::addUsage);

        sumByUnit(transactions, TransactionType.DISPOSAL)
                .forEach(report::addDisposal);
    }

    private Map<MedicinePackageUnit, Integer> sumByUnit(List<StockTransaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getTransactionType() == type)
                .collect(Collectors.groupingBy(StockTransaction::getMedicinePackageUnit, Collectors.summingInt(StockTransaction::getQuantity)));
    }
}
