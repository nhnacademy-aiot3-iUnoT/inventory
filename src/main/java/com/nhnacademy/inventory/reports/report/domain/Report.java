package com.nhnacademy.inventory.reports.report.domain;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.reports.report.exception.InvalidWeeklyPeriodException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "reports", uniqueConstraints = @UniqueConstraint(
        name = "uk_reports_organization_type_period",
        columnNames = {"organization_id", "report_type", "period_start"}
))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_summary_status", nullable = false)
    private AiSummaryStatus aiSummaryStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReportItem> reportItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Report(long organizationId, ReportType reportType, LocalDate periodStart, LocalDate periodEnd, AiSummaryStatus aiSummaryStatus) {
        this.organizationId = organizationId;
        this.reportType = reportType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.aiSummaryStatus = (aiSummaryStatus != null) ? aiSummaryStatus : AiSummaryStatus.PENDING;
    }

    public static Report weeklyOf(long organizationId, LocalDate periodStart) {
        if (!Objects.equals(periodStart.getDayOfWeek(), DayOfWeek.MONDAY)) {
            throw new InvalidWeeklyPeriodException();
        }

        return Report.builder()
                .organizationId(organizationId)
                .reportType(ReportType.WEEKLY)
                .periodStart(periodStart)
                .periodEnd(periodStart.plusDays(6))
                .aiSummaryStatus(AiSummaryStatus.PENDING)
                .build();
    }

    public void updateSummary(String aiSummary) {
        this.aiSummary = aiSummary;
        this.aiSummaryStatus = AiSummaryStatus.COMPLETED;
    }

    public void failSummary() {
        this.aiSummaryStatus = AiSummaryStatus.FAILED;
    }

    public void resetSummaryToPending() {
        this.aiSummaryStatus = AiSummaryStatus.PENDING;
    }

    public void addInbound(MedicinePackageUnit unit, int quantity) {
        this.reportItems.add(ReportItem.inbound(this, unit, quantity));
    }

    public void addOutbound(MedicinePackageUnit unit, int quantity) {
        this.reportItems.add(ReportItem.outbound(this, unit, quantity));
    }

    public void addDisposal(MedicinePackageUnit unit, int quantity) {
        this.reportItems.add(ReportItem.disposal(this, unit, quantity));
    }
}
