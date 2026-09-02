package com.nhnacademy.inventory.inventories.alert.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.alert.dto.*;
import com.nhnacademy.inventory.inventories.alert.service.AlertService;
import jakarta.validation.Valid;
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

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<PageResponse<AlertInfoResponse>>> getAlerts(
            AlertSearchCondition condition,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<AlertInfoResponse> responses = alertService.getAlerts(condition, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responses)));
    }

    @GetMapping("/alerts/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUncheckedAlertCount(){
        long count = alertService.getUncheckedAlertCount();

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/alerts/check")
    public ResponseEntity<Void> markAsChecked(
            @RequestBody AlertCheckRequest request
    ){
        alertService.markAsChecked(request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/alerts")
    public ResponseEntity<Void> deleteAlerts(
            @RequestBody AlertDeleteRequest request
    ){
        alertService.deleteAlerts(request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/alerts/all")
    public ResponseEntity<Void> deleteAllAlerts(){
        alertService.deleteAllAlerts();

        return ResponseEntity.noContent().build();
    }
}
