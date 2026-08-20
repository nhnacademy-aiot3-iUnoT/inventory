package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.OrganizationMemberValidator;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nhnacademy.inventory.reports.report.domain.ReportType;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportGetUseCase {

    private final ReportService reportService;
    private final OrganizationMemberValidator memberValidator;

    @Transactional(readOnly = true)
    public ReportInfoResponse getWeeklyReportById(UUID accountUuid, Long reportId) {
        OrganizationMember member = memberValidator.validateAndGet(accountUuid);
        Long organizationId = member.getOrganization().getId();

        Report report = reportService.getReport(reportId, organizationId);

        return ReportInfoResponse.of(report);
    }

    @Transactional(readOnly = true)
    public ReportInfoResponse getWeeklyReportByPeriod(UUID accountUuid, LocalDate periodStart) {
        OrganizationMember member = memberValidator.validateAndGet(accountUuid);
        Long organizationId = member.getOrganization().getId();

        Report report = reportService.find(organizationId, ReportType.WEEKLY, periodStart)
                .orElse(null);

        return (report != null) ? ReportInfoResponse.of(report) : null;
    }
}
