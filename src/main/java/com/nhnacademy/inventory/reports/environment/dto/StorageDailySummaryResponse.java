package com.nhnacademy.inventory.reports.environment.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 룰엔진의 저장소 하루치 환경 요약 응답
 */
public record StorageDailySummaryResponse(
        Long storageId,
        LocalDate date,

        // 그날 데이터가 있었던 구역만 담김
        List<ZoneDailySummaryResponse> zones
) {
}
