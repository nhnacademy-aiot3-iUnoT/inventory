package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.report.domain.Report;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportRetrySummaryUseCaseTest {

    @Mock
    private ReportService reportService;

    @Mock
    private StorageService storageService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReportRetrySummaryUseCase retrySummaryUseCase;

    @Test
    @DisplayName("이미 PENDING 상태인 경우 이벤트를 중복 발행하지 않고 현재 리포트를 반환한다.")
    void execute_WhenStatusIsPending_SkipsEventPublishing() {
        // given
        long organizationId = 1L;
        long storageId = 1L;
        long reportId = 1L;
        Report report = Report.weeklyOf(organizationId, storageId, LocalDate.of(2026, Month.AUGUST, 10));

        Storage storage = mock(Storage.class);

        given(storageService.validateMemberAndGetStorage(storageId))
                .willReturn(storage);
        given(storage.getId())
                .willReturn(storageId);
        given(reportService.getReportByStorage(reportId, storageId)).willReturn(report);

        // when
        retrySummaryUseCase.execute(storageId, reportId);

        // then
        then(eventPublisher)
                .should(never())
                .publishEvent(any());
        then(reportService)
                .should(never())
                .resetSummary(any());
    }

    @Test
    @DisplayName("FAILED 또는 COMPLETED 상태인 경우 상태를 PENDING으로 리셋하고 이벤트를 재발행한다.")
    void execute_WhenStatusIsFailed_ResetsAndPublishesEvent() {
        // given
        long organizationId = 1L;
        long storageId = 1L;
        long reportId = 1L;
        Report report = Report.weeklyOf(organizationId, storageId, LocalDate.of(2026, Month.AUGUST, 10));

        Storage storage = mock(Storage.class);

        given(storageService.validateMemberAndGetStorage(storageId))
                .willReturn(storage);
        given(storage.getId())
                .willReturn(storageId);
        given(reportService.getReportByStorage(reportId, storageId))
                .willReturn(report);
        report.failSummary();

        // when
        retrySummaryUseCase.execute(storageId, reportId);

        // then
        then(reportService)
                .should()
                .resetSummary(reportId);
        then(eventPublisher)
                .should()
                .publishEvent(any(ReportCreatedEvent.class));
    }
}
