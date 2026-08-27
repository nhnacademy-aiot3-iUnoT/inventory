package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.event.ReportGenerationRequestedEvent;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.ReportItemCollector;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportRecreateUseCase {

    private final ReportService reportService;
    private final ReportItemCollector reportItemCollector;
    private final ApplicationEventPublisher eventPublisher;
    private final StorageService storageService;

    @Transactional
    public ReportInfoResponse recreateWeekly(Long storageId, Long reportId) {
        Storage storage = storageService.validateMemberAndGetStorage(storageId);
        Report report = reportService.getReportByStorage(reportId, storage.getId());

        if (report.isAlreadyGeneratingSummary()) {
            log.info("리포트가 존재하지 않거나 AI 요약이 진행 중이므로 AI 요약 생성을 건너뜁니다. reportId={}", reportId);
            return ReportInfoResponse.from(report);
        }

        report.reset();

        reportItemCollector.collectReportItems(report);

        eventPublisher.publishEvent(new ReportGenerationRequestedEvent(report.getId()));

        return ReportInfoResponse.from(report);
    }
}
