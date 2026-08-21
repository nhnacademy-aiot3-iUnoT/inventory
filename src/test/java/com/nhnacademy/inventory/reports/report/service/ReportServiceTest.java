package com.nhnacademy.inventory.reports.report.service;

import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.exception.ReportNotFoundException;
import com.nhnacademy.inventory.reports.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("존재하지 않는 리포트를 ID로 조회하면 ReportNotFoundException이 발생한다.")
    void getReportWithId_WhenNotExists_ThrowsException() {
        // given
        long reportId = 1L;

        given(reportRepository.findByIdWithItems(reportId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.getReport(reportId))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 리포트의 AI 요약을 업데이트하면 ReportNotFoundException이 발생한다.")
    void updateSummary_WhenNotExists_ThrowsException() {
        // given
        long reportId = 1L;
        String summary = "AI 요약입니다.";

        given(reportRepository.findById(reportId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.updateSummary(reportId, summary))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    @DisplayName("AI 요약이 null이거나 빈 값이라면 업데이트하지 않는다.")
    void updateSummary_WhenSummaryNullOrEmpty_DoesNothing() {
        // given
        long reportId = 1L;
        Report report = mock(Report.class);

        given(reportRepository.findById(reportId))
                .willReturn(Optional.of(report));

        // when
        reportService.updateSummary(reportId, null);
        reportService.updateSummary(reportId, "");

        // then
        then(report)
                .should(never())
                .updateSummary(any());
    }

    @Test
    @DisplayName("존재하지 않는 리포트의 AI 요약을 실패 처리하면 예외가 발생한다.")
    void failSummary_WhenNotExists_ThrowsException() {
        // given
        long reportId = 1L;

        given(reportRepository.findById(reportId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.failSummary(reportId))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 리포트의 AI 요약을 초기화하면 예외가 발생한다.")
    void resetSummary_WhenNotExists_ThrowsException() {
        // given
        long reportId = 1L;

        given(reportRepository.findById(reportId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.resetSummary(reportId))
                .isInstanceOf(ReportNotFoundException.class);
    }
}