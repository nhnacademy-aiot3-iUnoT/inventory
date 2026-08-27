package com.nhnacademy.inventory.reports.environment.usecase;

import com.nhnacademy.inventory.reports.environment.client.RuleEngineApiClient;
import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentDoorStat;
import com.nhnacademy.inventory.reports.environment.domain.ReportEnvironmentStat;
import com.nhnacademy.inventory.reports.environment.dto.StorageDailySummaryResponse;
import com.nhnacademy.inventory.reports.environment.service.ReportEnvironmentService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEnvironmentCollectUseCase {

    private final ReportService reportService;
    private final ReportEnvironmentService reportEnvironmentService;
    private final RuleEngineApiClient ruleEngineApiClient;

    /**
     * 리포트 기간의 환경 데이터를 룰엔진에서 수집해 저장한다.
     * 룰엔진 호출은 외부 HTTP 통신이므로 트랜잭션 밖에서 하고, 초기화 및 저장만 트랜잭션으로 묶는다.
     */
    public void execute(Long reportId) {
        Report report = reportService.getReportWithoutItem(reportId);

        List<StorageDailySummaryResponse> summaries = ruleEngineApiClient.findDailySummaries(
                report.getStorageId(),
                report.getPeriodStart(),
                report.getPeriodEnd());

        if (summaries.isEmpty()) {
            log.info("수집된 환경 데이터가 없습니다. reportId={}", reportId);
            return;
        }

        reportEnvironmentService.registerReportEnvironment(
                reportId,
                toEnvironmentStats(report, summaries),
                toDoorStats(report, summaries));
    }

    private List<ReportEnvironmentStat> toEnvironmentStats(Report report, List<StorageDailySummaryResponse> summaries) {
        return summaries.stream()
                .flatMap(day -> day.zones().stream()
                        .flatMap(zone -> zone.sensorStats().stream()
                                .map(stat -> ReportEnvironmentStat.of(
                                        report,
                                        zone.zoneId(),
                                        day.date(),
                                        stat.sensorType(),
                                        stat.unit(),
                                        stat.avg(),
                                        stat.min(),
                                        stat.max(),
                                        stat.thresholdMin(),
                                        stat.thresholdMax(),
                                        stat.outOfRangeRatio()
                                ))))
                .toList();
    }

    private List<ReportEnvironmentDoorStat> toDoorStats(Report report, List<StorageDailySummaryResponse> summaries) {
        return summaries.stream()
                .flatMap(day -> day.zones().stream()
                        .filter(zone -> zone.door() != null)
                        .map(zone -> ReportEnvironmentDoorStat.of(
                                report,
                                zone.zoneId(),
                                day.date(),
                                zone.door().openCount(),
                                zone.door().openMinutes()
                        )))
                .toList();
    }
}
