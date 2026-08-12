package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import com.nhnacademy.inventory.reports.report.service.ReportSummaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportSummaryUseCaseTest {

    @Mock
    private ReportService reportService;

    @Mock
    private ReportSummaryService reportSummaryService;

    @InjectMocks
    private ReportSummaryUseCase reportSummaryUseCase;

    @Test
    @DisplayName("리포트 의약품 항목이 비어 있으면 AI 요약을 생성하지 않는다.")
    void execute_WhenEmptyReportItems_DoesNothing() {
        // given
        long reportId = 1L;
        Report report = Report.weeklyOf(1L, LocalDate.of(2026, Month.AUGUST, 10));

        given(reportService.getReport(reportId))
                .willReturn(report);

        // when
        reportSummaryUseCase.execute(reportId);

        // then
        then(reportSummaryService)
                .should(never())
                .generateSummary(anyString());
        then(reportService)
                .should(never())
                .updateSummary(eq(reportId), anyString());
    }
}