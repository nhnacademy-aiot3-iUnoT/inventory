package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportItem;
import com.nhnacademy.inventory.reports.report.domain.ReportItemType;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import com.nhnacademy.inventory.reports.report.service.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportSummaryUseCase {

    private final ReportService reportService;
    private final ReportSummaryService reportSummaryService;

    public void execute(Long reportId) {
        Report report = reportService.getReport(reportId);

        if (report.getReportItems().isEmpty()) {
            reportService.updateSummary(reportId, "해당 주간에는 출고 및 폐기 내역이 없습니다.");
            return;
        }

        try {
            String summary = reportSummaryService.generateSummary(toPromptText(report));

            if (summary == null || summary.isBlank()) {
                log.warn("AI 요약 결과가 비어 있음: {}", reportId);
                reportService.failSummary(reportId);
            }

            reportService.updateSummary(reportId, summary);
        } catch (Exception e) {
            log.error("AI 요약 생성 실패 reportId={}", reportId, e);
            reportService.failSummary(reportId);
        }
    }

    private String toPromptText(Report report) {
        String inboundText = toItemLines(report, ReportItemType.INBOUND);
        String outboundText = toItemLines(report, ReportItemType.OUTBOUND);
        String disposalText = toItemLines(report, ReportItemType.DISPOSAL);

        String reportType = report.getReportType().getName();

        return """
            기간: %s ~ %s (%s)
            
            [입고]
            %s
            
            [사용]
            %s
            
            [폐기]
            %s
            """.formatted(report.getPeriodStart(), report.getPeriodEnd(), reportType, inboundText, outboundText, disposalText);
    }

    private String toItemLines(Report report, ReportItemType type) {
        String text = report.getReportItems().stream()
                .filter(item -> item.getReportItemType() == type)
                .sorted(Comparator.comparing(ReportItem::getQuantity).reversed())
                .map(this::toItemLine)
                .collect(Collectors.joining("\n"));

        return (text.isBlank()) ? "없음" : text;
    }

    private String toItemLine(ReportItem item) {
        return String.format("%s / %s : %d개", item.getMedicineName(), item.getPackUnit(), item.getQuantity());
    }
}
