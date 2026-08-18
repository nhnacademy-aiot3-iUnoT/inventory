package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.reports.report.domain.AiSummaryStatus;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.dto.ReportCreatedEvent;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.OrganizationMemberValidator;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportRetrySummaryUseCase {

    private final ReportService reportService;
    private final OrganizationMemberValidator memberValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReportInfoResponse execute(UUID accountUuid, Long reportId) {
        OrganizationMember member = memberValidator.validateAndGet(accountUuid);
        Long organizationId = member.getOrganization().getId();

        Report report = reportService.getReport(reportId, organizationId);

        // 이미 PENDING 상태라면 중복 이벤트 발행 방지
        if (report.getAiSummaryStatus() == AiSummaryStatus.PENDING) {
            log.info("리포트(reportId={})의 AI 요약이 이미 진행 중(PENDING)이므로 재시도 요청을 건너뜁니다.", reportId);
            return ReportInfoResponse.of(report);
        }

        // 상태를 다시 PENDING으로 초기화
        reportService.resetSummary(reportId);

        // 비동기 AI 요약 이벤트 재발행
        eventPublisher.publishEvent(new ReportCreatedEvent(report.getId()));

        return ReportInfoResponse.of(report);
    }
}
