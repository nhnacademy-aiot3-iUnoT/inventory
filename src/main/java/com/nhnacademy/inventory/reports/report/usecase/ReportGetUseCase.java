package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentAggregator;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReportGetUseCase {

    private final ReportService reportService;
    private final StorageService storageService;
    private final ReportEnvironmentService reportEnvironmentService;
    private final ReportEnvironmentAggregator reportEnvironmentAggregator;

    @Transactional(readOnly = true)
    public ReportInfoResponse getWeeklyReportById(Long storageId, Long reportId) {
        Storage storage = storageService.validateMemberAndGetStorage(storageId);

        Report report = reportService.getReportByStorage(reportId, storage.getId());

        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public Optional<ReportInfoResponse> getWeeklyReportByPeriod(Long storageId, LocalDate periodStart) {
        Storage storage = storageService.validateMemberAndGetStorage(storageId);

        return reportService.find(storage.getId(), ReportType.WEEKLY, periodStart)
                .map(this::toResponse);
    }

    private ReportInfoResponse toResponse(Report report) {
        return ReportInfoResponse.from(
                report,
                reportEnvironmentAggregator.aggregate(reportEnvironmentService.getStats(report.getId())),
                reportEnvironmentAggregator.aggregateDoor(reportEnvironmentService.getDoorStats(report.getId())));
    }
}
