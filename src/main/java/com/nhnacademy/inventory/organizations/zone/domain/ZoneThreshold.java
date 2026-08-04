package com.nhnacademy.inventory.organizations.zone.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "zone_thresholds",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_zone_sensor",
                columnNames = {"zone_id", "sensor_type_id"}
            )})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZoneThreshold {

    @Id
    @Column(name = "zone_threshold_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long zoneThresholdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_type_id", nullable = false)
    private SensorType sensorType;

    @Column(name = "min_value", precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 10, scale = 2)
    private BigDecimal maxValue;

    @Column(name = "alert_duration", nullable = false)
    private Integer alertDuration;

    @Builder
    private ZoneThreshold(Zone zone, SensorType sensorType, BigDecimal minValue, BigDecimal maxValue, Integer alertDuration) {
        this.zone = zone;
        this.sensorType = sensorType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.alertDuration = alertDuration;
    }

    public void updateValues(BigDecimal minValue, BigDecimal maxValue, Integer alertDuration){
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.alertDuration = alertDuration;
    }
}
