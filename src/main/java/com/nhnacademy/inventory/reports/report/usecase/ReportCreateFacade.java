package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportCreateFacade {
    private final WeeklyReportCreateUseCase weeklyReportCreateUseCase;

    @Retryable(
            includes = {DataIntegrityViolationException.class},
            maxRetries = 1
    )
    public ReportInfoResponse createWeeklyReport(UUID accountUuid, LocalDate periodStart) {
        return weeklyReportCreateUseCase.execute(accountUuid, periodStart);
    }
}
