package com.nhnacademy.inventory.reports.environment.dto;

import java.util.List;

/**
 * 룰엔진의 구역 하루치 환경 요약 응답
 */
public record ZoneDailySummaryResponse(
        Long zoneId,
        List<SensorDailyStatResponse> sensorStats,

        // 문 센서가 없거나 하루 동안 기록이 없으면 null
        // 0회로 채우면 "문이 안 열렸다"와 "문 센서가 없다"가 구분되지 않음
        DoorDailyStatResponse door
) {
}
