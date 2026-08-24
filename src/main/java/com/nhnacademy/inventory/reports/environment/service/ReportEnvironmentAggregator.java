package com.nhnacademy.inventory.reports.environment.service;

import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentDoorStat;
import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentStat;
import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentDoorSummary;
import com.nhnacademy.inventory.reports.environment.dto.ReportEnvironmentSummary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportEnvironmentAggregator {

    private static final int VALUE_SCALE = 2;

    public List<ReportEnvironmentSummary> aggregate(List<ReportEnvironmentStat> stats) {
        return stats.stream()
                .collect(Collectors.groupingBy(
                        stat -> new SensorKey(stat.getZoneId(), stat.getSensorType()),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .map(entry -> toSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator
                        .comparing(ReportEnvironmentSummary::zoneId)
                        .thenComparing(ReportEnvironmentSummary::sensorType))
                .toList();
    }

    public List<ReportEnvironmentDoorSummary> aggregateDoor(List<ReportEnvironmentDoorStat> doorStats) {
        return doorStats.stream()
                .collect(Collectors.groupingBy(
                        ReportEnvironmentDoorStat::getZoneId,
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .map(entry -> toDoorSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ReportEnvironmentDoorSummary::zoneId))
                .toList();
    }

    private ReportEnvironmentSummary toSummary(SensorKey key, List<ReportEnvironmentStat> dailyStats) {
        List<ReportEnvironmentStat> sorted = dailyStats.stream()
                .sorted(Comparator.comparing(ReportEnvironmentStat::getStatDate))
                .toList();

        ReportEnvironmentStat latest = sorted.get(sorted.size() - 1);

        long outOfRangeDays = sorted.stream()
                .map(ReportEnvironmentStat::getOutOfRangeRatio)
                .filter(java.util.Objects::nonNull)
                .filter(ratio -> ratio.compareTo(BigDecimal.ZERO) > 0)
                .count();

        return new ReportEnvironmentSummary(
                key.zoneId(),
                key.sensorType(),
                latest.getUnit(),
                average(sorted.stream().map(ReportEnvironmentStat::getAvgValue).toList()),
                sorted.stream().map(ReportEnvironmentStat::getMinValue).min(BigDecimal::compareTo).orElse(null),
                sorted.stream().map(ReportEnvironmentStat::getMaxValue).max(BigDecimal::compareTo).orElse(null),
                latest.getThresholdMin(),
                latest.getThresholdMax(),
                (int) outOfRangeDays,
                sorted.size(),
                sorted.stream()
                        .map(stat -> new ReportEnvironmentSummary.DailyPoint(
                                stat.getStatDate(),
                                stat.getAvgValue(),
                                stat.getMinValue(),
                                stat.getMaxValue()))
                        .toList());
    }

    private ReportEnvironmentDoorSummary toDoorSummary(Long zoneId, List<ReportEnvironmentDoorStat> dailyStats) {
        List<ReportEnvironmentDoorStat> sorted = dailyStats.stream()
                .sorted(Comparator.comparing(ReportEnvironmentDoorStat::getStatDate))
                .toList();

        return new ReportEnvironmentDoorSummary(
                zoneId,
                sorted.stream().mapToLong(ReportEnvironmentDoorStat::getOpenCount).sum(),
                sorted.stream().mapToLong(ReportEnvironmentDoorStat::getOpenMinutes).sum(),
                sorted.size(),
                sorted.stream()
                        .map(stat -> new ReportEnvironmentDoorSummary.DailyPoint(
                                stat.getStatDate(),
                                stat.getOpenCount(),
                                stat.getOpenMinutes()))
                        .toList());
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return null;
        }

        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), VALUE_SCALE, RoundingMode.HALF_UP);
    }

    private record SensorKey(Long zoneId, String sensorType) {
    }
}
