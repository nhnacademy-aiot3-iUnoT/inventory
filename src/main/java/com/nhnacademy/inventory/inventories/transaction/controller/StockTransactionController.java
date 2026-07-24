package com.nhnacademy.inventory.inventories.transaction.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchCondition;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchResponse;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockTransactionService stockTransactionService;

    @GetMapping("/zones/{zoneId}/stock-transaction")
    public ResponseEntity<ApiResponse<Page<StockTransactionSearchResponse>>> search(
            @PathVariable Long zoneId,
            StockTransactionSearchCondition condition,
            @PageableDefault(size = 20, sort = "processedAt", direction = Sort.Direction.DESC)Pageable pageable
            ){

        return ResponseEntity.ok(ApiResponse.success(
                stockTransactionService.searchTransactionByCondition(
                        zoneId, condition, pageable)
        ));
    }
}
