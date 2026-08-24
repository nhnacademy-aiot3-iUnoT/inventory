package com.nhnacademy.inventory.reports.environment.dto;

/**
 * 룰엔진의 센서 타입별 하루 통계 응답
 * 평균, 이탈 비율 같은 수치는 룰엔진에서 이미 확정되어 오므로 여기서 다시 계산하지 않움
 */
public record SensorDailyStatResponse(
        String sensorType,
        String unit,
        double avg,
        double min,
        double max,

        // 구역에 임계값이 설정되지 않았거나 한쪽 경계만 설정된 경우 null
        Double thresholdMin,
        Double thresholdMax,

        // 임계 범위를 벗어난 측정값의 비율(0.0~1.0), 임계값이 없거나 표본이 없으면 null
        Double outOfRangeRatio,

        // 전일 데이터가 없으면 null이다.
        Double previousDayAvg
) {
}
