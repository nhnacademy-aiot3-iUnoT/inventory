package com.nhnacademy.inventory.dashboards.controller;

import com.nhnacademy.inventory.dashboards.dto.DashboardDepartmentsResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardExpiringResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardSummaryResponse;
import com.nhnacademy.inventory.dashboards.service.DashboardDepartmentService;
import com.nhnacademy.inventory.dashboards.service.DashboardEnvironmentService;
import com.nhnacademy.inventory.dashboards.service.DashboardExpiringService;
import com.nhnacademy.inventory.dashboards.service.DashboardSummaryService;
import com.nhnacademy.inventory.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/core/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardSummaryService dashboardSummaryService;
    private final DashboardDepartmentService dashboardDepartmentService;
    private final DashboardExpiringService dashboardExpiringService;
    private final DashboardEnvironmentService dashboardEnvironmentService;

    /**
     * 부서 선택 드롭다운. 권한에 따라 그룹이 달라진다.
     */
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<DashboardDepartmentsResponse>> getDepartments() {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardDepartmentService.getDepartmentOptions()));
    }

    /**
     * 부서의 입고,출고,유통기한 임박,환경이탈 임방 알림 조회
     * departmentId 를 생략시 조직 전체로 조회
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate target = date != null ? date : LocalDate.now();

        return ResponseEntity.ok(
                ApiResponse.success(dashboardSummaryService.getSummary(departmentId, target)));
    }

    /**
     * 유통기한 임박 의약품 목록 + D-7 / D-30 카운트.
     */
    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<DashboardExpiringResponse>> getExpiring(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer withinDays,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardExpiringService.getExpiring(departmentId, withinDays, size)));
    }

    /**
     * 선택한 저장소의 구역 센서 현황.
     */
    @GetMapping("/environment")
    public ResponseEntity<ApiResponse<DashboardEnvironmentResponse>> getEnvironment(
            @RequestParam Long storageId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardEnvironmentService.getEnvironment(storageId)));
    }
}
