package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentDoorSummary;
import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentSummary;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentAggregator;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.service.ReportPromptBuilder;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import com.nhnacademy.inventory.reports.report.service.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportSummaryUseCase {

    private final ReportService reportService;
    private final ReportSummaryService reportSummaryService;
    private final ReportEnvironmentService reportEnvironmentService;
    private final ReportEnvironmentAggregator reportEnvironmentAggregator;
    private final ReportPromptBuilder reportPromptBuilder;

    public void execute(Long reportId) {
        if (!reportService.acquireSummary(reportId)) {
            log.info("AI 요약이 이미 진행 중이므로 건너뜁니다: reportId={}", reportId);
            return;
        }

        try {
            Report report = reportService.getReport(reportId);

            List<ReportEnvironmentSummary> environments =
                    reportEnvironmentAggregator.aggregate(reportEnvironmentService.getStats(reportId));
            List<ReportEnvironmentDoorSummary> doors =
                    reportEnvironmentAggregator.aggregateDoor(reportEnvironmentService.getDoorStats(reportId));

            // 재고 변동 내역과 환경 데이터가 모두 없을 때만 요약을 생략
            boolean skipSummary = report.getReportItems().isEmpty()
                    && environments.isEmpty()
                    && doors.isEmpty();

            if (skipSummary) {
                reportService.updateSummary(reportId, "해당 주간에는 재고 변동 및 환경 측정 내역이 없습니다.");
                return;
            }

            String prompt = reportPromptBuilder.toPromptText(report, environments, doors);
            String summary = reportSummaryService.generateSummary(prompt);

            if (summary == null || summary.isBlank()) {
                log.warn("AI 요약 결과가 비어 있음: {}", reportId);
                reportService.failSummary(reportId);
                return;
            }

            reportService.updateSummary(reportId, summary);
        } catch (Exception e) {
            log.error("AI 요약 생성 실패 reportId={}", reportId, e);
            reportService.failSummary(reportId);
        }
    }
}
