package com.nhnacademy.inventory.reports.report.dto;

import com.nhnacademy.inventory.reports.report.validator.PastWeekMonday;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReportCreateRequest(
        @NotNull(message = "리포트 시작일은 필수입니다.")
        @PastWeekMonday
        LocalDate periodStart
) {}
