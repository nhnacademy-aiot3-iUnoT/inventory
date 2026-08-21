package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nhnacademy.inventory.reports.report.domain.ReportType;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReportGetUseCase {
    private final ReportService reportService;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public ReportInfoResponse getWeeklyReportById(Long storageId, Long reportId) {
        Storage storage = storageService.validateMemberAndGetStorage(storageId);

        Report report = reportService.getReportByStorage(reportId, storage.getId());

        return ReportInfoResponse.from(report);
    }

    @Transactional(readOnly = true)
    public Optional<ReportInfoResponse> getWeeklyReportByPeriod(Long storageId, LocalDate periodStart) {
        Storage storage = storageService.validateMemberAndGetStorage(storageId);

        return reportService.find(storage.getId(), ReportType.WEEKLY, periodStart)
                .map(ReportInfoResponse::from);
    }
}
