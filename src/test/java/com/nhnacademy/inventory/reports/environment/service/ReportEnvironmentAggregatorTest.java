package com.nhnacademy.inventory.reports.environment.service;

import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentDoorStat;
import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentStat;
import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentDoorSummary;
import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentSummary;
import com.nhnacademy.inventory.reports.report.domain.Report;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportEnvironmentAggregatorTest {

    private static final LocalDate PERIOD_START = LocalDate.of(2026, Month.AUGUST, 10);

    private final ReportEnvironmentAggregator aggregator = new ReportEnvironmentAggregator();

    private final Report report = Report.weeklyOf(1L, 1L, PERIOD_START);

    @Test
    @DisplayName("구역과 센서 종류가 같은 일별 데이터를 하나로 묶는다.")
    void aggregate_GroupsByZoneAndSensorType() {
        // given
        List<ReportEnvironmentStat> stats = List.of(
                stat(1L, "TEMPERATURE", 0, 4.0, 3.0, 5.0, 2.0, 8.0, 0.0),
                stat(1L, "TEMPERATURE", 1, 6.0, 5.0, 9.0, 2.0, 8.0, 0.2),
                stat(1L, "HUMIDITY", 0, 40.0, 38.0, 42.0, null, null, null),
                stat(2L, "TEMPERATURE", 0, 20.0, 19.0, 21.0, null, 30.0, 0.0));

        // when
        List<ReportEnvironmentSummary> summaries = aggregator.aggregate(stats);

        // then
        assertThat(summaries)
                .hasSize(3)
                .extracting(ReportEnvironmentSummary::zoneId, ReportEnvironmentSummary::sensorType)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, "HUMIDITY"),
                        org.assertj.core.groups.Tuple.tuple(1L, "TEMPERATURE"),
                        org.assertj.core.groups.Tuple.tuple(2L, "TEMPERATURE"));
    }

    @Test
    @DisplayName("평균은 일평균들의 평균이고, 범위는 기간 전체의 최저와 최고이다.")
    void aggregate_CalculatesAverageAndRange() {
        // given
        List<ReportEnvironmentStat> stats = List.of(
                stat(1L, "TEMPERATURE", 0, 4.0, 3.0, 5.0, 2.0, 8.0, 0.0),
                stat(1L, "TEMPERATURE", 1, 6.0, 5.0, 9.0, 2.0, 8.0, 0.2));

        // when
        ReportEnvironmentSummary summary = aggregator.aggregate(stats).get(0);

        // then
        assertThat(summary.avgValue()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(summary.minValue()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
        assertThat(summary.maxValue()).isEqualByComparingTo(BigDecimal.valueOf(9.0));
        assertThat(summary.measuredDays()).isEqualTo(2);
    }

    @Test
    @DisplayName("이탈 비율이 0보다 큰 날만 이탈 일수로 센다.")
    void aggregate_CountsOnlyDaysWithPositiveRatio() {
        // given: 0.0042는 아주 작지만 이탈이 있었던 날이다.
        List<ReportEnvironmentStat> stats = List.of(
                stat(1L, "TEMPERATURE", 0, 4.0, 3.0, 5.0, 2.0, 8.0, 0.0),
                stat(1L, "TEMPERATURE", 1, 5.0, 4.0, 8.2, 2.0, 8.0, 0.0042),
                stat(1L, "TEMPERATURE", 2, 7.0, 5.0, 9.9, 2.0, 8.0, 0.18));

        // when
        ReportEnvironmentSummary summary = aggregator.aggregate(stats).get(0);

        // then
        assertThat(summary.outOfRangeDays()).isEqualTo(2);
        assertThat(summary.hasOutOfRange()).isTrue();
    }

    @Test
    @DisplayName("이탈 비율이 null인 날은 이탈로 세지 않는다.")
    void aggregate_IgnoresNullRatio() {
        // given: null은 임계값이 없거나 표본이 없다는 뜻이므로 이탈 0%와 구분해야 한다.
        List<ReportEnvironmentStat> stats = List.of(
                stat(1L, "HUMIDITY", 0, 40.0, 38.0, 42.0, null, null, null),
                stat(1L, "HUMIDITY", 1, 41.0, 39.0, 43.0, null, null, null));

        // when
        ReportEnvironmentSummary summary = aggregator.aggregate(stats).get(0);

        // then
        assertThat(summary.outOfRangeDays()).isZero();
        assertThat(summary.hasThreshold()).isFalse();
    }

    @Test
    @DisplayName("임계값은 기간 중 바뀔 수 있으므로 가장 최근 날짜 기준으로 표기한다.")
    void aggregate_UsesLatestThreshold() {
        // given: 둘째 날에 상한이 8.0에서 6.0으로 바뀌었다.
        List<ReportEnvironmentStat> stats = List.of(
                stat(1L, "TEMPERATURE", 0, 4.0, 3.0, 5.0, 2.0, 8.0, 0.0),
                stat(1L, "TEMPERATURE", 1, 5.0, 4.0, 6.0, 2.0, 6.0, 0.0));

        // when
        ReportEnvironmentSummary summary = aggregator.aggregate(stats).get(0);

        // then
        assertThat(summary.thresholdMax()).isEqualByComparingTo(BigDecimal.valueOf(6.0));
    }

    @Test
    @DisplayName("차트용 일별 값은 날짜 오름차순으로 정렬한다.")
    void aggregate_SortsDailyPointsByDate() {
        // given: 입력 순서를 일부러 뒤섞는다.
        List<ReportEnvironmentStat> stats = List.of(
                stat(1L, "TEMPERATURE", 2, 6.0, 5.0, 7.0, 2.0, 8.0, 0.0),
                stat(1L, "TEMPERATURE", 0, 4.0, 3.0, 5.0, 2.0, 8.0, 0.0),
                stat(1L, "TEMPERATURE", 1, 5.0, 4.0, 6.0, 2.0, 8.0, 0.0));

        // when
        ReportEnvironmentSummary summary = aggregator.aggregate(stats).get(0);

        // then
        assertThat(summary.dailyPoints())
                .extracting(ReportEnvironmentSummary.DailyPoint::date)
                .containsExactly(
                        PERIOD_START,
                        PERIOD_START.plusDays(1),
                        PERIOD_START.plusDays(2));
    }

    @Test
    @DisplayName("문 개폐는 구역별로 횟수와 시간을 합산한다.")
    void aggregateDoor_SumsPerZone() {
        // given
        List<ReportEnvironmentDoorStat> doorStats = List.of(
                doorStat(1L, 0, 12, 18),
                doorStat(1L, 1, 15, 22),
                doorStat(2L, 0, 3, 4));

        // when
        List<ReportEnvironmentDoorSummary> summaries = aggregator.aggregateDoor(doorStats);

        // then
        assertThat(summaries).hasSize(2);

        ReportEnvironmentDoorSummary zone1 = summaries.get(0);
        assertThat(zone1.zoneId()).isEqualTo(1L);
        assertThat(zone1.totalOpenCount()).isEqualTo(27);
        assertThat(zone1.totalOpenMinutes()).isEqualTo(40);
        assertThat(zone1.measuredDays()).isEqualTo(2);
    }

    @Test
    @DisplayName("데이터가 없으면 빈 목록을 반환한다.")
    void aggregate_WhenEmpty_ReturnsEmptyList() {
        assertThat(aggregator.aggregate(List.of())).isEmpty();
        assertThat(aggregator.aggregateDoor(List.of())).isEmpty();
    }

    private ReportEnvironmentStat stat(
            Long zoneId, String sensorType, int dayOffset,
            double avg, double min, double max,
            Double thresholdMin, Double thresholdMax, Double outOfRangeRatio
    ) {
        return ReportEnvironmentStat.of(
                report, zoneId, PERIOD_START.plusDays(dayOffset),
                sensorType, "°C",
                avg, min, max,
                thresholdMin, thresholdMax, outOfRangeRatio);
    }

    private ReportEnvironmentDoorStat doorStat(Long zoneId, int dayOffset, long openCount, long openMinutes) {
        return ReportEnvironmentDoorStat.of(
                report, zoneId, PERIOD_START.plusDays(dayOffset), openCount, openMinutes);
    }
}
