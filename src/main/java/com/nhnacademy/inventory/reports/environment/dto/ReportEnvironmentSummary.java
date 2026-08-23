package com.nhnacademy.inventory.reports.environment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// 기간 전체의 환경 요약
public record ReportEnvironmentSummary(
        Long zoneId,
        String sensorType,
        String unit,

        // 전체 기간 평균
        BigDecimal avgValue,

        // 전체 기잔 중 최저 / 최고
        BigDecimal minValue,
        BigDecimal maxValue,

        BigDecimal thresholdMin,
        BigDecimal thresholdMax,

        int outOfRangeDays,
        int measuredDays,

        // 차트용 일별 값
        List<DailyPoint> dailyPoints
) {

    public record DailyPoint(
            LocalDate date,
            BigDecimal avgValue,
            BigDecimal minValue,
            BigDecimal maxValue
    ) {
    }

    public boolean hasThreshold() {
        return thresholdMin != null || thresholdMax != null;
    }

    public boolean hasOutOfRange() {
        return outOfRangeDays > 0;
    }
}
