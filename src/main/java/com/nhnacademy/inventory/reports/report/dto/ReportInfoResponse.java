package com.nhnacademy.inventory.reports.report.dto;

import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReportInfoResponse(
        Long reportId,
        Long organizationId,
        ReportType reportType,
        LocalDate periodStart,
        LocalDate periodEnd,
        String aiSummary,
        LocalDateTime createdAt,
        List<ReportItemResponse> items
) {
    public static ReportInfoResponse of(Report report) {
        return new ReportInfoResponse(
                report.getId(),
                report.getOrganizationId(),
                report.getReportType(),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getAiSummary(),
                report.getCreatedAt(),
                report.getReportItems().stream()
                        .map(ReportItemResponse::from)
                        .toList()
        );
    }
}
