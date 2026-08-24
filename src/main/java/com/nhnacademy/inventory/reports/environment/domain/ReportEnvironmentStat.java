package com.nhnacademy.inventory.reports.environment.domain;

import com.nhnacademy.inventory.reports.report.domain.Report;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "report_environment_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportEnvironmentStat {

    // 온도, 습도, 조도 같은 측정값의 자릿수
    private static final int MEASURE_PRECISION = 10;
    private static final int MEASURE_SCALE = 2;

    // 이탈 비율(0.0~1.0)의 자릿수
    private static final int RATIO_PRECISION = 5;
    private static final int RATIO_SCALE = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_environment_stat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    // 구역은 이름이 바뀌어도 현재 이름을 따라가야 현장에서 찾을 수 있으므로 스냅샷하지 않음
    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "sensor_type", length = 30, nullable = false)
    private String sensorType;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "avg_value", nullable = false, precision = MEASURE_PRECISION, scale = MEASURE_SCALE)
    private BigDecimal avgValue;

    @Column(name = "min_value", nullable = false, precision = MEASURE_PRECISION, scale = MEASURE_SCALE)
    private BigDecimal minValue;

    @Column(name = "max_value", nullable = false, precision = MEASURE_PRECISION, scale = MEASURE_SCALE)
    private BigDecimal maxValue;

    // null은 임계값 미설정을 뜻함, 한쪽 경계만 설정된 구역이 있어 각각 null일 수 있음
    @Column(name = "threshold_min", precision = MEASURE_PRECISION, scale = MEASURE_SCALE)
    private BigDecimal thresholdMin;

    @Column(name = "threshold_max", precision = MEASURE_PRECISION, scale = MEASURE_SCALE)
    private BigDecimal thresholdMax;

    @Column(name = "out_of_range_ratio", precision = RATIO_PRECISION, scale = RATIO_SCALE)
    private BigDecimal outOfRangeRatio;

    private ReportEnvironmentStat(
            Report report, Long zoneId, String sensorType, LocalDate statDate, String unit,
            BigDecimal avgValue, BigDecimal minValue, BigDecimal maxValue,
            BigDecimal thresholdMin, BigDecimal thresholdMax, BigDecimal outOfRangeRatio
    ) {
        this.report = report;
        this.zoneId = zoneId;
        this.sensorType = sensorType;
        this.statDate = statDate;
        this.unit = unit;
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.thresholdMin = thresholdMin;
        this.thresholdMax = thresholdMax;
        this.outOfRangeRatio = outOfRangeRatio;
    }

    public static ReportEnvironmentStat of(
            Report report, Long zoneId, LocalDate statDate,
            String sensorType, String unit,
            double avg, double min, double max,
            Double thresholdMin, Double thresholdMax, Double outOfRangeRatio
    ) {
        return new ReportEnvironmentStat(
                report, zoneId, sensorType, statDate, unit,
                BigDecimal.valueOf(avg),
                BigDecimal.valueOf(min),
                BigDecimal.valueOf(max),
                toBigDecimal(thresholdMin),
                toBigDecimal(thresholdMax),
                toBigDecimal(outOfRangeRatio)
        );
    }

    private static BigDecimal toBigDecimal(Double value) {
        return (value != null) ? BigDecimal.valueOf(value) : null;
    }
}
