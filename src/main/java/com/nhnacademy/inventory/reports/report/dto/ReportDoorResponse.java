package com.nhnacademy.inventory.reports.report.dto;

import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentDoorSummary;

import java.time.LocalDate;
import java.util.List;

public record ReportDoorResponse(
        Long zoneId,
        long totalOpenCount,
        long totalOpenMinutes,
        int measuredDays,
        List<DailyPointResponse> dailyPoints
) {

    public record DailyPointResponse(
            LocalDate date,
            long openCount,
            long openMinutes
    ) {
        public static DailyPointResponse from(ReportEnvironmentDoorSummary.DailyPoint point) {
            return new DailyPointResponse(point.date(), point.openCount(), point.openMinutes());
        }
    }

    public static ReportDoorResponse from(ReportEnvironmentDoorSummary summary) {
        return new ReportDoorResponse(
                summary.zoneId(),
                summary.totalOpenCount(),
                summary.totalOpenMinutes(),
                summary.measuredDays(),
                summary.dailyPoints().stream()
                        .map(DailyPointResponse::from)
                        .toList());
    }
}
