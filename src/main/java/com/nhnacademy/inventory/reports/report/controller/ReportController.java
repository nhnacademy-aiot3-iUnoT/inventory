package com.nhnacademy.inventory.reports.report.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.reports.report.dto.ReportCreateRequest;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.usecase.ReportCreateFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class ReportController {
    private final ReportCreateFacade reportCreateFacade;

    @PostMapping("/organizations/{organization-id}/reports/weekly")
    public ResponseEntity<ApiResponse<ReportInfoResponse>> createWeeklyReport(
            @PathVariable(name = "organization-id") Long organizationId,
            @RequestBody ReportCreateRequest request
    ) {
        ReportInfoResponse response = reportCreateFacade.createWeeklyReport(organizationId, request.periodStart());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
