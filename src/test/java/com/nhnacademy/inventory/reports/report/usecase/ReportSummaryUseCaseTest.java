package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentStat;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentAggregator;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import com.nhnacademy.inventory.reports.report.service.ReportSummaryService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportSummaryUseCaseTest {

    private static final LocalDate PERIOD_START = LocalDate.of(2026, Month.AUGUST, 10);

    @Mock
    private ReportService reportService;

    @Mock
    private ReportSummaryService reportSummaryService;

    @Mock
    private ReportEnvironmentService reportEnvironmentService;

    // 집계 결과가 프롬프트에 반영되는지 확인하는 것이 목적이므로 실제 집계기 사용
    @Spy
    private ReportEnvironmentAggregator reportEnvironmentAggregator = new ReportEnvironmentAggregator();

    @InjectMocks
    private ReportSummaryUseCase reportSummaryUseCase;

    @Test
    @DisplayName("재고 변동과 환경 데이터가 모두 없으면 안내 메시지로 완료 처리한다.")
    void execute_WhenNoItemsAndNoEnvironment_SetsEmptyMessage() {
        // given
        long reportId = 1L;
        Report report = Report.weeklyOf(1L, 1L, PERIOD_START);

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
                .updateSummary(reportId, "해당 주간에는 재고 변동 및 환경 측정 내역이 없습니다.");
    }

    @Test
    @DisplayName("재고 변동이 없어도 환경 데이터가 있으면 요약을 생성한다.")
    void execute_WhenOnlyEnvironmentExists_GeneratesSummary() {
        // given
        long reportId = 1L;
        Report report = Report.weeklyOf(1L, 1L, PERIOD_START);

        given(reportService.getReport(reportId))
                .willReturn(report);
        given(reportEnvironmentService.getStats(reportId))
                .willReturn(List.of(temperatureStat(report)));
        given(reportSummaryService.generateSummary(anyString()))
                .willReturn("요약 결과");

        // when
        reportSummaryUseCase.execute(reportId);

        // then
        then(reportService)
                .should()
                .updateSummary(reportId, "요약 결과");
    }

    @Test
    @DisplayName("환경 데이터가 프롬프트에 구역·센서 단위로 집계되어 포함된다.")
    void execute_IncludesAggregatedEnvironmentInPrompt() {
        // given
        long reportId = 1L;
        Report report = Report.weeklyOf(1L, 1L, PERIOD_START);

        given(reportService.getReport(reportId))
                .willReturn(report);
        given(reportEnvironmentService.getStats(reportId))
                .willReturn(List.of(temperatureStat(report)));
        given(reportSummaryService.generateSummary(anyString()))
                .willReturn("요약 결과");

        // when
        reportSummaryUseCase.execute(reportId);

        // then
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        then(reportSummaryService)
                .should()
                .generateSummary(promptCaptor.capture());

        assertThat(promptCaptor.getValue())
                .contains("[환경]")
                .contains("구역 10 / TEMPERATURE(°C)")
                .contains("기준 2.0~8.0");
    }

    @Test
    @DisplayName("AI 요약 생성 중 예외가 발생하면 failSummary를 호출한다.")
    void execute_WhenExceptionOccurs_CallsFailSummary() {
        // given
        long reportId = 1L;
        Report report = Report.weeklyOf(1L, 1L, PERIOD_START);
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

    private ReportEnvironmentStat temperatureStat(Report report) {
        return ReportEnvironmentStat.of(
                report, 10L, PERIOD_START,
                "TEMPERATURE", "°C",
                5.2, 3.1, 8.4,
                2.0, 8.0, 0.0);
    }
}
