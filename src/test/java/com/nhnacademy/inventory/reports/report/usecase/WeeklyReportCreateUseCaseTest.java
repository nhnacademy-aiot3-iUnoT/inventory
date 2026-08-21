package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.dto.ReportCreatedEvent;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WeeklyReportCreateUseCaseTest {

    @Mock
    private ReportService reportService;

    @Mock
    private StockTransactionService stockTransactionService;

    @Mock
    private StorageService storageService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WeeklyReportCreateUseCase weeklyReportCreateUseCase;

    @Test
    @DisplayName("해당 날짜에 이미 생성된 리포트가 있다면, 리포트를 생성하지 않고 해당 리포트를 반환한다.")
    void execute_WhenReportExist_DoesNotGenerate() {
        // given
        long organizationId = 1L;
        long storageId = 1L;
        LocalDate periodStart = LocalDate.of(2026, Month.AUGUST, 10);
        Report report = Report.weeklyOf(organizationId, storageId, periodStart);

        Organization organization = mock(Organization.class);
        Storage storage = mock(Storage.class);

        given(storageService.validateMemberAndGetStorage(storageId))
                .willReturn(storage);
        given(storage.getOrganization())
                .willReturn(organization);
        given(organization.getId())
                .willReturn(organizationId);
        given(reportService.find(storageId, ReportType.WEEKLY, periodStart))
                .willReturn(Optional.of(report));

        // when
        weeklyReportCreateUseCase.execute(storageId, periodStart);

        // then
        then(reportService)
                .should(never())
                .register(any(Report.class));
        then(eventPublisher)
                .should(never())
                .publishEvent(any(ReportCreatedEvent.class));
    }

    @Test
    @DisplayName("해당 날짜에 생성된 리포트가 없다면, 리포트를 생성하고 리포트 생성 이벤트를 발행한다.")
    void execute_WhenReportNotExist_GenerateReportAndPublishEvent() {
        // given
        long organizationId = 1L;
        long storageId = 1L;
        LocalDate periodStart = LocalDate.of(2026, Month.AUGUST, 10);
        Report report = Report.weeklyOf(organizationId, storageId, periodStart);

        Organization organization = mock(Organization.class);
        Storage storage = mock(Storage.class);

        given(storageService.validateMemberAndGetStorage(storageId))
                .willReturn(storage);
        given(storage.getOrganization())
                .willReturn(organization);
        given(storage.getId())
                .willReturn(storageId);
        given(organization.getId())
                .willReturn(organizationId);
        given(reportService.find(storageId, ReportType.WEEKLY, periodStart))
                .willReturn(Optional.empty());
        given(reportService.register(any(Report.class)))
                .willReturn(report);
        given(stockTransactionService.findTransactionsForStorageReport(
                storageId,
                List.of(TransactionType.INBOUND, TransactionType.OUTBOUND, TransactionType.DISPOSAL),
                report.getPeriodStart(),
                report.getPeriodEnd()))
                .willReturn(List.of());

        // when
        weeklyReportCreateUseCase.execute(storageId, periodStart);

        // then
        then(reportService)
                .should()
                .register(any(Report.class));
        then(eventPublisher)
                .should()
                .publishEvent(any(ReportCreatedEvent.class));
    }
}