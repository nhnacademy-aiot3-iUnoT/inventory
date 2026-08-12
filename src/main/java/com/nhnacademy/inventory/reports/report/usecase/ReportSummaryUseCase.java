package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportItem;
import com.nhnacademy.inventory.reports.report.domain.ReportItemType;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import com.nhnacademy.inventory.reports.report.service.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportSummaryUseCase {

    private final ReportService reportService;
    private final ReportSummaryService reportSummaryService;

    public void execute(Long reportId) {
        Report report = reportService.getReport(reportId);

        if (report.getReportItems().isEmpty()) {
            return;
        }

        String summary = reportSummaryService.generateSummary(toPromptText(report));

        reportService.updateSummary(reportId, summary);
    }

    private String toPromptText(Report report) {
        String usageText = toItemLines(report, ReportItemType.USAGE);
        String disposalText = toItemLines(report, ReportItemType.DISPOSAL);

        String reportType = report.getReportType().getName();

        return """
            기간: %s ~ %s (%s)
            
            [사용]
            %s
            
            [폐기]
            %s
            """.formatted(report.getPeriodStart(), report.getPeriodEnd(), reportType, usageText, disposalText);
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
