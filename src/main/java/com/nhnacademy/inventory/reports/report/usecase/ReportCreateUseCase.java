package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.event.ReportGenerationRequestedEvent;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.ReportItemCollector;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportCreateUseCase {

    private final ReportService reportService;
    private final ReportItemCollector reportItemCollector;
    private final ApplicationEventPublisher eventPublisher;
    private final StorageService storageService;

    @Transactional
    public ReportInfoResponse createWeekly(Long storageId, LocalDate periodStart) {
        Storage storage = storageService.validateMemberAndGetStorage(storageId);
        long organizationId = storage.getOrganization().getId();

        Report report = reportService.find(storageId, ReportType.WEEKLY, periodStart)
                .orElseGet(() -> generate(organizationId, storage.getId(), periodStart));

        return ReportInfoResponse.from(report);
    }

    private Report generate(Long organizationId, Long storageId, LocalDate periodStart) {
        Report report = Report.weeklyOf(organizationId, storageId, periodStart);
        Report saved = reportService.register(report);

        reportItemCollector.collectReportItems(saved);

        eventPublisher.publishEvent(new ReportGenerationRequestedEvent(saved.getId()));

        return saved;
    }
}
