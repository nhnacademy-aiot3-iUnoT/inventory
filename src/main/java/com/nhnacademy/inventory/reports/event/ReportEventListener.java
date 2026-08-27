package com.nhnacademy.inventory.reports.event;

import com.nhnacademy.inventory.reports.environment.usecase.ReportEnvironmentCollectUseCase;
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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportGenerateRequested(ReportGenerationRequestedEvent event) {
        reportEnvironmentCollectUseCase.execute(event.reportId());

        reportSummaryUseCase.execute(event.reportId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportSummaryRequested(ReportSummaryRequestedEvent event) {
        reportSummaryUseCase.execute(event.reportId());
    }
}
