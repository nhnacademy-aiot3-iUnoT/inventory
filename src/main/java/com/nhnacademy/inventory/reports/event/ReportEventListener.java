package com.nhnacademy.inventory.reports.event;

import com.nhnacademy.inventory.reports.environment.usecase.ReportEnvironmentCollectUseCase;
import com.nhnacademy.inventory.reports.report.dto.ReportCreatedEvent;
import com.nhnacademy.inventory.reports.report.usecase.ReportSummaryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEventListener {

    private final ReportEnvironmentCollectUseCase reportEnvironmentCollectUseCase;
    private final ReportSummaryUseCase reportSummaryUseCase;

    // 리스너를 둘로 나누면 실행 순서가 보장되지 않는다.
    // AI 요약이 환경 데이터를 근거로 쓰려면 수집이 먼저 끝나 있어야 하므로 한 리스너에서 순서대로 호출한다.
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportCreated(ReportCreatedEvent event) {
        collectEnvironment(event.reportId());

        reportSummaryUseCase.execute(event.reportId());
    }

    // 환경 데이터는 부수적인 정보이므로, 수집에 실패해도 AI 요약은 진행한다.
    private void collectEnvironment(Long reportId) {
        try {
            reportEnvironmentCollectUseCase.execute(reportId);
        } catch (Exception e) {
            log.warn("환경 데이터 수집에 실패했습니다. reportId={}", reportId, e);
        }
    }
}
