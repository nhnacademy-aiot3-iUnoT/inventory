package com.nhnacademy.inventory.inventories.expiration.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventorySearchRequest;
import com.nhnacademy.inventory.inventories.expiration.service.ExpirationSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class ExpiringSearchController {

    private final ExpirationSearchService expirationSearchService;

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<PageResponse<ExpiringInventoryResponse>>> searchExpiringInventories(
            ExpiringInventorySearchRequest request,
            @PageableDefault(size = 20) Pageable pageable
    ){
        Page<ExpiringInventoryResponse> responses = expirationSearchService.searchExpiringInventories(request, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responses)));
    }
}
