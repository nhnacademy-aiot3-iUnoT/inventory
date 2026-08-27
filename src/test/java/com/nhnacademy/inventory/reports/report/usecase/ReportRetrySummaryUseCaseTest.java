package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.event.ReportSummaryRequestedEvent;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReportRetrySummaryUseCaseTest {

    @Mock
    private ReportService reportService;

    @Mock
    private StorageService storageService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReportRetrySummaryUseCase retrySummaryUseCase;

    @Test
    @DisplayName("리포트가 존재하면, 리포트 재생성 이벤트를 발행한다.")
    void execute_WhenReportExists_PublishesEvent() {
        // given
        long storageId = 1L;
        long reportId = 1L;
        Storage storage = mock(Storage.class);
        Report report = mock(Report.class);

        given(storageService.validateMemberAndGetStorage(storageId))
                .willReturn(storage);
        given(storage.getId())
                .willReturn(storageId);
        given(reportService.getReportByStorage(reportId, storage.getId()))
                .willReturn(report);

        // when
        retrySummaryUseCase.execute(storageId, reportId);

        // then
        then(eventPublisher)
                .should()
                .publishEvent(any(ReportSummaryRequestedEvent.class));
    }
}
