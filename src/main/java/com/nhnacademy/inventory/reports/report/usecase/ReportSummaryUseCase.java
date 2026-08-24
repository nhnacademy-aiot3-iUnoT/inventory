package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentDoorSummary;
import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentSummary;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentAggregator;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportItem;
import com.nhnacademy.inventory.reports.report.domain.ReportItemType;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import com.nhnacademy.inventory.reports.report.service.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportSummaryUseCase {

    private static final int DISPLAY_SCALE = 1;

    private final ReportService reportService;
    private final ReportSummaryService reportSummaryService;
    private final ReportEnvironmentService reportEnvironmentService;
    private final ReportEnvironmentAggregator reportEnvironmentAggregator;

    public void execute(Long reportId) {
        Report report = reportService.getReport(reportId);

        List<ReportEnvironmentSummary> environments =
                reportEnvironmentAggregator.aggregate(reportEnvironmentService.getStats(reportId));
        List<ReportEnvironmentDoorSummary> doors =
                reportEnvironmentAggregator.aggregateDoor(reportEnvironmentService.getDoorStats(reportId));

        // 재고 변동과 환경 데이터가 모두 없을 때만 요약을 생략
        if (report.getReportItems().isEmpty() && environments.isEmpty() && doors.isEmpty()) {
            reportService.updateSummary(reportId, "해당 주간에는 재고 변동 및 환경 측정 내역이 없습니다.");
            return;
        }

        try {
            String summary = reportSummaryService.generateSummary(toPromptText(report, environments, doors));

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

    private String toPromptText(
            Report report,
            List<ReportEnvironmentSummary> environments,
            List<ReportEnvironmentDoorSummary> doors
    ) {
        return """
            기간: %s ~ %s (%s)

            [입고]
            %s

            [사용]
            %s

            [폐기]
            %s

            [환경]
            %s
            """.formatted(
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getReportType().getName(),
                toItemLines(report, ReportItemType.INBOUND),
                toItemLines(report, ReportItemType.OUTBOUND),
                toItemLines(report, ReportItemType.DISPOSAL),
                toEnvironmentLines(environments, doors));
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

    private String toEnvironmentLines(
            List<ReportEnvironmentSummary> environments,
            List<ReportEnvironmentDoorSummary> doors
    ) {
        String text = Stream.concat(
                        environments.stream().map(this::toSensorLine),
                        doors.stream().map(this::toDoorLine))
                .collect(Collectors.joining("\n"));

        return text.isBlank() ? "없음" : text;
    }

    private String toSensorLine(ReportEnvironmentSummary summary) {
        return "구역 %d / %s%s : 평균 %s, 범위 %s~%s, 기준 %s, %s".formatted(
                summary.zoneId(),
                summary.sensorType(),
                summary.unit() == null ? "" : "(" + summary.unit() + ")",
                format(summary.avgValue()),
                format(summary.minValue()),
                format(summary.maxValue()),
                formatThreshold(summary),
                formatOutOfRange(summary));
    }

    private String toDoorLine(ReportEnvironmentDoorSummary summary) {
        return "구역 %d / 문 개폐 : 총 %d회, 누적 개방 %d분 (%d일 측정)".formatted(
                summary.zoneId(),
                summary.totalOpenCount(),
                summary.totalOpenMinutes(),
                summary.measuredDays());
    }

    // 임계값은 한쪽만 설정될 수 있고, null은 설정되지 않았다는 정보이므로 구분해서 표기
    private String formatThreshold(ReportEnvironmentSummary summary) {
        if (!summary.hasThreshold()) {
            return "미설정";
        }

        return "%s~%s".formatted(
                summary.thresholdMin() == null ? "제한없음" : format(summary.thresholdMin()),
                summary.thresholdMax() == null ? "제한없음" : format(summary.thresholdMax()));
    }

    private String formatOutOfRange(ReportEnvironmentSummary summary) {
        if (!summary.hasOutOfRange()) {
            return "이탈 없음";
        }

        return "이탈 %d일/%d일".formatted(summary.outOfRangeDays(), summary.measuredDays());
    }

    private String format(BigDecimal value) {
        return (value == null) ? "-" : value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
