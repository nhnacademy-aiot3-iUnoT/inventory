package com.nhnacademy.inventory.reports.report.service;

import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.exception.ReportNotFoundException;
import com.nhnacademy.inventory.reports.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public Optional<Report> find(Long storageId, ReportType reportType, LocalDate periodStart) {
        return reportRepository.findByStorageIdAndReportTypeAndPeriodStartWithItems(
                storageId, reportType, periodStart);
    }

    @Transactional
    public Report register(Report report) {
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public Report getReport(Long reportId) {
        return reportRepository.findByIdWithItems(reportId)
                .orElseThrow(ReportNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Report getReportByStorage(Long reportId, Long storageId) {
        return reportRepository.findByIdAndStorageId(reportId, storageId)
                .orElseThrow(ReportNotFoundException::new);
    }

    @Transactional
    public void updateSummary(Long reportId, String aiSummary) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        if (aiSummary != null && !aiSummary.isBlank()) {
            report.updateSummary(aiSummary);
        }
    }

    @Transactional
    public void failSummary(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        report.failSummary();
    }

    @Transactional
    public void resetSummary(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        report.resetSummaryToPending();
    }
}
