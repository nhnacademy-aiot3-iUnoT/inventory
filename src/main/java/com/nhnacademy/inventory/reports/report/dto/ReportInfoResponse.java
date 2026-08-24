package com.nhnacademy.inventory.reports.report.dto;

import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentDoorSummary;
import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentSummary;
import com.nhnacademy.inventory.reports.report.domain.AiSummaryStatus;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReportInfoResponse(
        Long reportId,
        Long organizationId,
        Long storageId,
        ReportType reportType,
        LocalDate periodStart,
        LocalDate periodEnd,
        String aiSummary,
        AiSummaryStatus aiSummaryStatus,
        LocalDateTime createdAt,
        List<ReportItemResponse> items,
        List<ReportEnvironmentResponse> environments,
        List<ReportDoorResponse> doors
) {

    // 리포트 생성 직후에 사용할 리포트만 담은 데이터
    public static ReportInfoResponse from(Report report) {
        return from(report, List.of(), List.of());
    }

    public static ReportInfoResponse from(
            Report report,
            List<ReportEnvironmentSummary> environments,
            List<ReportEnvironmentDoorSummary> doors
    ) {
        if (report == null) {
            return null;
        }

        return new ReportInfoResponse(
                report.getId(),
                report.getOrganizationId(),
                report.getStorageId(),
                report.getReportType(),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getAiSummary(),
                report.getAiSummaryStatus(),
                report.getCreatedAt(),
                report.getReportItems().stream()
                        .map(ReportItemResponse::from)
                        .toList(),
                environments.stream()
                        .map(ReportEnvironmentResponse::from)
                        .toList(),
                doors.stream()
                        .map(ReportDoorResponse::from)
                        .toList()
        );
    }
}
