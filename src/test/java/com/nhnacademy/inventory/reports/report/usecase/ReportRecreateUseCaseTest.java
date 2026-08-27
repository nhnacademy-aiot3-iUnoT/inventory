package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.event.ReportGenerationRequestedEvent;
import com.nhnacademy.inventory.reports.report.domain.AiSummaryStatus;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.exception.ReportNotFoundException;
import com.nhnacademy.inventory.reports.report.service.ReportItemCollector;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportRecreateUseCaseTest {

    private static final LocalDate PERIOD_START = LocalDate.of(2026, Month.AUGUST, 10);
    private static final long STORAGE_ID = 1L;
    private static final long REPORT_ID = 1L;

    @Mock
    private ReportService reportService;

    @Mock
    private StorageService storageService;

    @Mock
    private ReportItemCollector reportItemCollector;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReportRecreateUseCase reportRecreateUseCase;

    @Test
    @DisplayName("재생성하면 기존 아이템과 AI 요약을 비우고 리포트 생성 이벤트를 발행한다.")
    void recreateWeekly_ResetsReportAndPublishesEvent() {
        // given
        Report report = completedReport();
        givenStorageAndReport(report);

        // when
        reportRecreateUseCase.recreateWeekly(STORAGE_ID, REPORT_ID);

        // then
        assertThat(report.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.PENDING);
        assertThat(report.getAiSummary()).isNull();
        // 수집기는 목이므로 다시 채우지 않는다. 즉 기존 아이템이 비워졌음을 의미한다.
        assertThat(report.getReportItems()).isEmpty();

        then(reportItemCollector)
                .should()
                .collectReportItems(report);
        then(eventPublisher)
                .should()
                .publishEvent(any(ReportGenerationRequestedEvent.class));
    }

    @Test
    @DisplayName("AI 요약이 이미 생성 중이면 재생성하지 않고 기존 리포트를 반환한다.")
    void recreateWeekly_WhenSummaryInProgress_DoesNotRecreate() {
        // given
        Report report = mock(Report.class);
        given(report.isAlreadyGeneratingSummary())
                .willReturn(true);
        givenStorageAndReport(report);

        // when
        reportRecreateUseCase.recreateWeekly(STORAGE_ID, REPORT_ID);

        // then
        then(report)
                .should(never())
                .reset();
        then(reportItemCollector)
                .should(never())
                .collectReportItems(any(Report.class));
        then(eventPublisher)
                .should(never())
                .publishEvent(any(ReportGenerationRequestedEvent.class));
    }

    @Test
    @DisplayName("저장소에 속하지 않은 리포트는 재생성할 수 없다.")
    void recreateWeekly_WhenReportNotInStorage_Throws() {
        // given
        Storage storage = mock(Storage.class);
        given(storageService.validateMemberAndGetStorage(STORAGE_ID))
                .willReturn(storage);
        given(storage.getId())
                .willReturn(STORAGE_ID);
        given(reportService.getReportByStorage(REPORT_ID, STORAGE_ID))
                .willThrow(new ReportNotFoundException());

        // when & then
        assertThatThrownBy(() -> reportRecreateUseCase.recreateWeekly(STORAGE_ID, REPORT_ID))
                .isInstanceOf(ReportNotFoundException.class);

        then(eventPublisher)
                .should(never())
                .publishEvent(any(ReportGenerationRequestedEvent.class));
    }

    private void givenStorageAndReport(Report report) {
        Storage storage = mock(Storage.class);

        given(storageService.validateMemberAndGetStorage(STORAGE_ID))
                .willReturn(storage);
        given(storage.getId())
                .willReturn(STORAGE_ID);
        given(reportService.getReportByStorage(REPORT_ID, STORAGE_ID))
                .willReturn(report);
    }

    private Report completedReport() {
        Report report = Report.weeklyOf(1L, STORAGE_ID, PERIOD_START);
        report.addOutbound(
                TestFixtures.createPackageUnit(TestFixtures.createMedicine("202106092", "타이레놀")), 10);
        report.updateSummary("이전 요약");

        return report;
    }
}
