package com.nhnacademy.inventory.dashboards.dto;

/**
 * 대시보드 상단 KPI 4장. 네 장이 모두 같은 모양이라 지표 하나로 통일한다.
 */
public record DashboardSummaryResponse(
        MetricResponse inbound,
        MetricResponse outbound,
        MetricResponse expiring,
        MetricResponse envAlert
) {

    /**
     * KPI 카드 한 장.
     *
     * @param value    큰 숫자 (입고 수량, 임박 품목 수 …)
     * @param subValue 숫자 옆 보조 수치 (거래 건수, 미확인 알림 수 …). 없으면 0
     * @param diff     전일 대비 증감. 화면은 절대값과 방향으로 나눠 표시한다
     * @param diffRate 전일 대비 증감률(%). 전일 값이 0이면 계산할 수 없어 null
     */
    public record MetricResponse(
            long value,
            long subValue,
            long diff,
            Double diffRate
    ) {
        public static MetricResponse of(long value, long subValue, long previous) {
            long diff = value - previous;
            Double rate = previous == 0L ? null : (diff * 100.0) / previous;

            return new MetricResponse(value, subValue, diff, rate);
        }

        public static MetricResponse zero() {
            return new MetricResponse(0L, 0L, 0L, null);
        }
    }

    public static DashboardSummaryResponse empty() {
        return new DashboardSummaryResponse(
                MetricResponse.zero(),
                MetricResponse.zero(),
                MetricResponse.zero(),
                MetricResponse.zero()
        );
    }
}
