package com.nhnacademy.inventory.reports.report.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.reports.report.dto.ReportCreateRequest;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.usecase.ReportCreateFacade;
import com.nhnacademy.inventory.reports.report.usecase.ReportGetUseCase;
import com.nhnacademy.inventory.reports.report.usecase.ReportRetrySummaryUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/core/storages/{storage-id}/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportCreateFacade reportCreateFacade;
    private final ReportGetUseCase reportGetUseCase;
    private final ReportRetrySummaryUseCase reportRetrySummaryUseCase;

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<ReportInfoResponse>> getWeeklyReport(
            @PathVariable(name = "storage-id") Long storageId,
            @RequestParam(name = "periodStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart
    ) {
        ReportInfoResponse response = reportGetUseCase.getWeeklyReportByPeriod(storageId, periodStart);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/weekly")
    public ResponseEntity<ApiResponse<ReportInfoResponse>> createWeeklyReport(
            @PathVariable(name = "storage-id") Long storageId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        ReportInfoResponse response = reportCreateFacade.createWeeklyReport(storageId, request.periodStart());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{report-id}")
    public ResponseEntity<ApiResponse<ReportInfoResponse>> getReport(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "report-id") Long reportId
    ) {
        ReportInfoResponse response = reportGetUseCase.getWeeklyReportById(storageId, reportId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{report-id}/ai-summary/retry")
    public ResponseEntity<Void> retryAiSummary(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "report-id") Long reportId
    ) {
        reportRetrySummaryUseCase.execute(storageId, reportId);

        return ResponseEntity.noContent().build();
    }
}
