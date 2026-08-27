package com.nhnacademy.inventory.enviroments.event.domain;

import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "environment_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnvironmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "environment_event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "detected_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal detectedValue;

    @Column(name = "threshold_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal thresholdValue;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "environment_type", length = 30, nullable = false)
    private EnvironmentType environmentType;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "breach_type", length = 30, nullable = false)
    private BreachType breachType;

    @Builder
    private EnvironmentEvent(Zone zone, BigDecimal detectedValue, BigDecimal thresholdValue,
                             EnvironmentType environmentType, BreachType breachType) {
        this.zone = zone;
        this.detectedValue = detectedValue;
        this.thresholdValue = thresholdValue;
        this.environmentType = environmentType;
        this.breachType = breachType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}