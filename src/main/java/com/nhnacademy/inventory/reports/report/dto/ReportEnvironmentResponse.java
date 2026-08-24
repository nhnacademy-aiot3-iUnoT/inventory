package com.nhnacademy.inventory.reports.report.dto;

import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// 프론트에 내려줄 구역 + 센서 단위 환경 요약
public record ReportEnvironmentResponse(
        Long zoneId,
        String sensorType,
        String unit,
        BigDecimal avgValue,
        BigDecimal minValue,
        BigDecimal maxValue,

        BigDecimal thresholdMin,
        BigDecimal thresholdMax,

        int outOfRangeDays,
        int measuredDays,
        List<DailyPointResponse> dailyPoints
) {

    public record DailyPointResponse(
            LocalDate date,
            BigDecimal avgValue,
            BigDecimal minValue,
            BigDecimal maxValue
    ) {
        public static DailyPointResponse from(ReportEnvironmentSummary.DailyPoint point) {
            return new DailyPointResponse(
                    point.date(),
                    point.avgValue(),
                    point.minValue(),
                    point.maxValue());
        }
    }

    public static ReportEnvironmentResponse from(ReportEnvironmentSummary summary) {
        return new ReportEnvironmentResponse(
                summary.zoneId(),
                summary.sensorType(),
                summary.unit(),
                summary.avgValue(),
                summary.minValue(),
                summary.maxValue(),
                summary.thresholdMin(),
                summary.thresholdMax(),
                summary.outOfRangeDays(),
                summary.measuredDays(),
                summary.dailyPoints().stream()
                        .map(DailyPointResponse::from)
                        .toList());
    }
}
