package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.OrganizationMemberValidator;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportGetUseCase {

    private final ReportService reportService;
    private final OrganizationMemberValidator memberValidator;

    @Transactional(readOnly = true)
    public ReportInfoResponse execute(UUID accountUuid, Long reportId) {
        OrganizationMember member = memberValidator.validateAndGet(accountUuid);
        Long organizationId = member.getOrganization().getId();

        Report report = reportService.getReport(reportId, organizationId);

        return ReportInfoResponse.of(report);
    }
}
