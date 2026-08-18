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
    public Optional<Report> find(Long organizationId, ReportType reportType, LocalDate periodStart) {
        return reportRepository.findByOrganizationIdAndReportTypeAndPeriodStart(
                organizationId, reportType, periodStart);
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
    public Report getReport(Long reportId, Long organizationId) {
        return reportRepository.findByIdAndOrganizationId(reportId, organizationId)
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
}
