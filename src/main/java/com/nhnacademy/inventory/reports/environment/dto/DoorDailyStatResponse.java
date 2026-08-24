package com.nhnacademy.inventory.reports.environment.dto;

/**
 * 룰엔진의 문 개폐 하루 통계 응답
 */
public record DoorDailyStatResponse(
        long openCount,
        long openMinutes
) {
}
