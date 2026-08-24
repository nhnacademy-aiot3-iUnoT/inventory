package com.nhnacademy.inventory.reports.environment.dto;

import java.time.LocalDate;
import java.util.List;

// 구역 단위로 묶은 기간 전체의 문 개폐 요약
public record ReportEnvironmentDoorSummary(
        Long zoneId,
        long totalOpenCount,
        long totalOpenMinutes,
        int measuredDays,

        // 차트용 일별 값
        List<DailyPoint> dailyPoints
) {

    public record DailyPoint(
            LocalDate date,
            long openCount,
            long openMinutes
    ) {
    }
}
