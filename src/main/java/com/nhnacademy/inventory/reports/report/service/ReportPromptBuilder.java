package com.nhnacademy.inventory.reports.report.service;

import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentDoorSummary;
import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentSummary;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportItem;
import com.nhnacademy.inventory.reports.report.domain.ReportItemType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReportPromptBuilder {
    private static final int DISPLAY_SCALE = 1;

    public String toPromptText(
            Report report,
            List<ReportEnvironmentSummary> environments,
            List<ReportEnvironmentDoorSummary> doors
    ) {
        String prompt = """
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
                toItemLines(report, ReportItemType.INBOUND, "입고량"),
                toItemLines(report, ReportItemType.OUTBOUND, "사용량"),
                toItemLines(report, ReportItemType.DISPOSAL, "폐기량"),
                toEnvironmentLines(environments, doors));

        log.debug("AI 프롬프트: {}", prompt);

        return prompt;
    }

    private String toItemLines(Report report, ReportItemType type, String totalLabel) {
        List<ReportItem> items = report.getReportItems().stream()
                .filter(item -> item.getReportItemType() == type)
                .sorted(Comparator.comparing(ReportItem::getQuantity).reversed())
                .toList();

        if (items.isEmpty()) {
            return "없음";
        }

        int total = items.stream().mapToInt(ReportItem::getQuantity).sum();

        return items.stream()
                .map(item -> toItemLine(item, total, totalLabel))
                .collect(Collectors.joining("\n"));
    }

    // LLM 에게 나눗셈을 맡기면 틀린 값을 그럴듯하게 써내기 때문에 비중을 여기서 계산함
    private String toItemLine(ReportItem item, int total, String totalLabel) {
        return "%s / %s : %d개 (%s의 %s%%)".formatted(
                item.getMedicineName(),
                item.getPackUnit(),
                item.getQuantity(),
                totalLabel,
                formatShare(item.getQuantity(), total));
    }

    private String formatShare(int quantity, int total) {
        if (total <= 0) {
            return "-";
        }

        return BigDecimal.valueOf(quantity * 100.0 / total)
                .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String toEnvironmentLines(
            List<ReportEnvironmentSummary> environments,
            List<ReportEnvironmentDoorSummary> doors
    ) {
        Map<Long, ReportEnvironmentDoorSummary> doorsByZone = doors.stream()
                .collect(Collectors.toMap(ReportEnvironmentDoorSummary::zoneId, Function.identity(), (a, b) -> a));

        List<String> lines = new ArrayList<>();

        for (ReportEnvironmentSummary summary : environments) {
            lines.add(toSensorLine(summary));

            // 이탈은 특정 날짜에 일어난 일이므로 주간 합계로는 근거를 댈 수 없음
            // 해당 구역의 일별 수치와 문 개폐 데이터를 함께 넘겨서 의미 있는 문장을 생성하도록 함
            if (summary.hasOutOfRange()) {
                lines.addAll(toDailyLines(summary, doorsByZone.get(summary.zoneId())));
            }
        }

        doors.stream()
                .map(this::toDoorLine)
                .forEach(lines::add);

        return lines.isEmpty() ? "없음" : String.join("\n", lines);
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

    private List<String> toDailyLines(ReportEnvironmentSummary summary, ReportEnvironmentDoorSummary door) {
        if (summary.dailyPoints() == null || summary.dailyPoints().isEmpty()) {
            return List.of();
        }

        Map<LocalDate, ReportEnvironmentDoorSummary.DailyPoint> doorByDate = (door == null || door.dailyPoints() == null)
                ? Map.of()
                : door.dailyPoints().stream()
                        .collect(Collectors.toMap(
                                ReportEnvironmentDoorSummary.DailyPoint::date, Function.identity(), (a, b) -> a));

        return summary.dailyPoints().stream()
                .map(point -> "  %s : 평균 %s, 범위 %s~%s%s".formatted(
                        point.date(),
                        format(point.avgValue()),
                        format(point.minValue()),
                        format(point.maxValue()),
                        formatDoorOfDay(doorByDate.get(point.date()))))
                .toList();
    }

    private String formatDoorOfDay(ReportEnvironmentDoorSummary.DailyPoint door) {
        return (door == null) ? "" : ", 문 개폐 %d회 %d분".formatted(door.openCount(), door.openMinutes());
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
        if (!summary.hasThreshold()) {
            return "판단 불가";
        }

        if (!summary.hasOutOfRange()) {
            return "이탈 없음";
        }

        return "이탈 %d일/%d일".formatted(summary.outOfRangeDays(), summary.measuredDays());
    }

    private String format(BigDecimal value) {
        return (value == null) ? "-" : value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
