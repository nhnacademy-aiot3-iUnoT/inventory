package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import com.nhnacademy.inventory.reports.report.service.ReportSummaryService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;

import static org.mockito.ArgumentMatchers.anyString;
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
    @DisplayName("리포트 의약품 항목이 비어 있으면 안내 메시지로 완료 처리한다.")
    void execute_WhenEmptyReportItems_SetsEmptyMessage() {
        // given
        long reportId = 1L;
        Report report = Report.weeklyOf(1L, 1L, LocalDate.of(2026, Month.AUGUST, 10));

        given(reportService.getReport(reportId))
                .willReturn(report);

        // when
        reportSummaryUseCase.execute(reportId);

        // then
        then(reportSummaryService)
                .should(never())
                .generateSummary(anyString());
        then(reportService)
                .should()
                .updateSummary(reportId, "해당 주간에는 출고 및 폐기 내역이 없습니다.");
    }

    @Test
    @DisplayName("AI 요약 생성 중 예외가 발생하면 failSummary를 호출한다.")
    void execute_WhenExceptionOccurs_CallsFailSummary() {
        // given
        long reportId = 1L;
        Report report = Report.weeklyOf(1L, 1L, LocalDate.of(2026, Month.AUGUST, 10));
        report.addOutbound(TestFixtures.createPackageUnit(TestFixtures.createMedicine("202106092", "타이레놀")), 10);

        given(reportService.getReport(reportId))
                .willReturn(report);
        given(reportSummaryService.generateSummary(anyString()))
                .willThrow(new RuntimeException("LLM API Timeout"));

        // when
        reportSummaryUseCase.execute(reportId);

        // then
        then(reportService)
                .should()
                .failSummary(reportId);
    }
}