package com.nhnacademy.inventory.reports.environment.domain;

import com.nhnacademy.inventory.reports.report.domain.Report;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "report_environment_door_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportEnvironmentDoorStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_environment_door_stat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    // 구역은 이름이 바뀌어도 현재 이름을 따라가야 현장에서 찾을 수 있으므로 스냅샷으로 저장하지 않음
    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "open_count", nullable = false)
    private long openCount;

    @Column(name = "open_minutes", nullable = false)
    private long openMinutes;

    private ReportEnvironmentDoorStat(Report report, Long zoneId, LocalDate statDate, long openCount, long openMinutes) {
        this.report = report;
        this.zoneId = zoneId;
        this.statDate = statDate;
        this.openCount = openCount;
        this.openMinutes = openMinutes;
    }

    // 문 센서가 없거나 기록이 없는 구역은 애초에 이 엔티티를 만들지 않음
    // 0회로 채우면 "문이 안 열렸다"와 "문 센서가 없다"가 구분되지 않음
    public static ReportEnvironmentDoorStat of(Report report, Long zoneId, LocalDate statDate, long openCount, long openMinutes) {
        return new ReportEnvironmentDoorStat(report, zoneId, statDate, openCount, openMinutes);
    }
}
