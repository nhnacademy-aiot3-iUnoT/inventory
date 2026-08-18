package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.service.OrganizationMemberValidator;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReportGetUseCaseTest {

    @Mock
    private ReportService reportService;

    @Mock
    private OrganizationMemberValidator memberValidator;

    @InjectMocks
    private ReportGetUseCase reportGetUseCase;

    @Test
    @DisplayName("리포트를 조회하면 사용자의 조직을 조회한 뒤 해당 값으로 조회를 시도한다.")
    void execute_WhenGetReport_ValidatesMemberAndGet() {
        // given
        UUID accountUuid = UUID.randomUUID();
        Organization organization = mock(Organization.class);
        OrganizationMember member = mock(OrganizationMember.class);
        long organizationId = 1L;
        long reportId = 1L;
        Report report = mock(Report.class);

        given(memberValidator.validateAndGet(accountUuid))
                .willReturn(member);
        given(member.getOrganization())
                .willReturn(organization);
        given(organization.getId())
                .willReturn(organizationId);
        given(reportService.getReport(reportId, organizationId))
                .willReturn(report);

        // when
        reportGetUseCase.execute(accountUuid, reportId);

        // then
        then(memberValidator)
                .should()
                .validateAndGet(accountUuid);
        then(reportService)
                .should()
                .getReport(reportId, organizationId);
    }
}