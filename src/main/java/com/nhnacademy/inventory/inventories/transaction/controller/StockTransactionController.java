package com.nhnacademy.inventory.inventories.transaction.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchCondition;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionInfoResponse;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockTransactionService stockTransactionService;

    @GetMapping("/zones/{zone-id}/stock-transaction")
    public ResponseEntity<ApiResponse<PageResponse<StockTransactionInfoResponse>>> search(
            @PathVariable("zone-id") Long zoneId,
            StockTransactionSearchCondition condition,
            @PageableDefault(size = 20, sort = "processedAt", direction = Sort.Direction.DESC)Pageable pageable
            ){
        Page<StockTransactionInfoResponse> responsePage =
                stockTransactionService.searchTransactionByCondition(zoneId, condition, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }
}
