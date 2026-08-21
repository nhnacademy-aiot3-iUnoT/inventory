package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReportGetUseCaseTest {

    @Mock
    private ReportService reportService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ReportGetUseCase reportGetUseCase;

    @Test
    @DisplayName("리포트를 조회하면 회원을 검증한 뒤 해당 저장소의 리포트 조회를 시도한다.")
    void getWeeklyReportById_WhenGetReport_ValidatesMemberAndGet() {
        // given
        long storageId = 1L;
        long reportId = 1L;
        Report report = mock(Report.class);
        Storage storage = mock(Storage.class);

        given(storageService.validateMemberAndGetStorage(storageId))
                .willReturn(storage);
        given(storage.getId())
                .willReturn(storageId);
        given(reportService.getReportByStorage(reportId, storageId))
                .willReturn(report);

        // when
        reportGetUseCase.getWeeklyReportById(storageId, reportId);

        // then
        then(reportService)
                .should()
                .getReportByStorage(reportId, storageId);
    }

    @Test
    @DisplayName("리포트가 존재하지 않으면 null을 반환한다.")
    void getWeeklyReportByPeriod_WhenNotFound_ReturnsNull() {
        // given
        long storageId = 1L;
        LocalDate periodStart = LocalDate.of(2026, Month.AUGUST, 20);

        Storage storage = mock(Storage.class);

        given(storageService.validateMemberAndGetStorage(storageId))
                .willReturn(storage);
        given(storage.getId())
                .willReturn(storageId);
        given(reportService.find(storageId, ReportType.WEEKLY, periodStart))
                .willReturn(Optional.empty());

        // when
        ReportInfoResponse result = reportGetUseCase.getWeeklyReportByPeriod(storageId, periodStart);

        // then
        assertThat(result)
                .isNull();
    }
}