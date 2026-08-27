package com.nhnacademy.inventory.enviroments.event.controller;

import com.nhnacademy.inventory.enviroments.event.dto.*;
import com.nhnacademy.inventory.enviroments.event.service.EnvironmentEventService;
import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class EnvironmentEventController {

    private final EnvironmentEventService environmentEventService;

    @PostMapping("/internal/environment-events")
    public ResponseEntity<Void> createEnvironmentEvent(
            @RequestBody @Valid EnvironmentEventCreateRequest request
    ){
        environmentEventService.createEnvironmentEvent(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/zones/{zone-id}/environment-events")
    public ResponseEntity<ApiResponse<PageResponse<EnvironmentEventInfoResponse>>> searchEnvironmentEvents(
            @PathVariable(name = "zone-id") Long zoneId,
            EnvironmentEventSearchCondition condition,
            @PageableDefault(size = 20)Pageable pageable
    ){
        Page<EnvironmentEventInfoResponse> responses = environmentEventService.searchEnvironmentEvents(zoneId, condition, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responses)));
    }

    @GetMapping("/environment-events")
    public ResponseEntity<ApiResponse<List<EnvironmentEventItemResponse>>> getEnvironmentEvents(
            @RequestBody @Valid EnvironmentEventCheckRequest request
    ){
        List<EnvironmentEventItemResponse> responses = environmentEventService.getEnvironmentEvents(request);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
