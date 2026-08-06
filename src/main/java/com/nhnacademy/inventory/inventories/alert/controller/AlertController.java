package com.nhnacademy.inventory.inventories.alert.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.alert.dto.*;
import com.nhnacademy.inventory.inventories.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/organizations/{organization-id}/alerts")
    public ResponseEntity<ApiResponse<PageResponse<AlertInfoResponse>>> getAlerts(
            @PathVariable(name = "organization-id") Long organizationId,
            AlertSearchCondition condition,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<AlertInfoResponse> responses = alertService.getAlerts(organizationId, condition, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responses)));
    }

    @GetMapping("/organizations/{organization-id}/alerts/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadAlertCount(
            @PathVariable(name = "organization-id") Long organizationId
    ){
        long count = alertService.getUnreadAlertCount(organizationId);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/organizations/{organization-id}/alerts/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable(name = "organization-id") Long organizationId,
            @RequestBody AlertReadRequest request
    ){
        alertService.markAsRead(organizationId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/organizations/{organization-id}/alerts")
    public ResponseEntity<Void> deleteAlerts(
            @PathVariable(name = "organization-id") Long organizationId,
            @RequestBody AlertDeleteRequest request
    ){
        alertService.deleteAlerts(organizationId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/organizations/{organization-id}/alerts/all")
    public ResponseEntity<Void> deleteAllAlerts(
            @PathVariable(name = "organization-id") Long organizationId
    ){
        alertService.deleteAllAlerts(organizationId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("internal/alerts")                             //내부 서버 전용 api
    public ResponseEntity<Void> createAlert(
            @RequestBody AlertCreateRequest request
    ){
        alertService.createAlert(request.organizationId(), request.alertType(), request.message());

        return ResponseEntity.noContent().build();
    }
}
