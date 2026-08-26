package com.nhnacademy.inventory.reports.report.repository;

import com.nhnacademy.inventory.global.config.QuerydslConfig;
import com.nhnacademy.inventory.reports.report.domain.AiSummaryStatus;
import com.nhnacademy.inventory.reports.report.domain.Report;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class})
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Test
    @DisplayName("AI 요약 생성 상태가 PROGRESS가 아니면 획득에 성공하고, 이미 PROGRESS면 실패한다.")
    void acquireSummary_WhenAiSummaryStatusNotPROGRESS_AcquiresSuccess() {
        // given
        Report report = reportRepository.save(Report.weeklyOf(1L, 1L, LocalDate.of(2026, Month.AUGUST, 10)));

        // when & then
        assertThat(reportRepository.acquireSummary(report.getId(), AiSummaryStatus.PROGRESS))
                .isEqualTo(1);
        assertThat(reportRepository.acquireSummary(report.getId(), AiSummaryStatus.PROGRESS))
                .isZero();
        assertThat(reportRepository.findById(report.getId()))
                .get()
                .extracting(Report::getAiSummaryStatus)
                .isEqualTo(AiSummaryStatus.PROGRESS);
    }

    @Test
    @DisplayName("AI 요약 생성 상태가 COMPLETED나 FAILED 상태에서는 다시 획득할 수 있다.")
    void acquireSummary_WhenAiSummaryStatusCompletedOrFailed_AcquiresSuccess() {
        // given
        Report report = reportRepository.save(Report.weeklyOf(1L, 1L, LocalDate.of(2026, Month.AUGUST, 10)));

        report.updateSummary("테스트 요약");
        reportRepository.flush();

        // when & then
        assertThat(reportRepository.acquireSummary(report.getId(), AiSummaryStatus.PROGRESS))
                .isEqualTo(1);
    }
}