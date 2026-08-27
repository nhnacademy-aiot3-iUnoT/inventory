package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.event.ReportSummaryRequestedEvent;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportRetrySummaryUseCase {

    private final ReportService reportService;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(Long storageId, Long reportId) {
        Storage storage = storageService.validateMemberAndGetStorage(storageId);
        Report report = reportService.getReportByStorage(reportId, storage.getId());

        eventPublisher.publishEvent(new ReportSummaryRequestedEvent(report.getId()));
    }
}
