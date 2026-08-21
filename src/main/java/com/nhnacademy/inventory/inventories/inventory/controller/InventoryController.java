package com.nhnacademy.inventory.inventories.inventory.controller;


import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryInfoResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.service.InboundService;
import com.nhnacademy.inventory.inventories.inventory.service.InventoriesSearchService;
import com.nhnacademy.inventory.inventories.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/core/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InboundService inboundService;
    private final InventoriesSearchService inventoriesSearchService;
    private final InventoryService inventoryService;


    // 입고 등록 - 환경기준 등록
    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody MedicineInboundRequest request){

        inboundService.createInbound(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
        // 기존 입고 수량 증가 포함한다는 의미라면 200 ok
    }


    // 전체 재고 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoriesResponse>>> getInventories(@RequestParam(name= "search",required = false) String search,
                                                                                         @RequestParam(name= "storage-id",required = false) Long storageId,
                                                                                         Pageable pageable){

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(inventoriesSearchService.getInventories(search,storageId,pageable))));

    }

    // 상세 재고 조회
    @GetMapping("/storages/{storage-id}/package-units/{medicine-package-unit-id}")
    public ResponseEntity<ApiResponse<PageResponse<InventoryInfoResponse>>> getInventoryInfo(@PathVariable(name = "storage-id")Long storageId,
                                                                                                   @PathVariable(name = "medicine-package-unit-id")Long packUnitId,
                                                                                                   Pageable pageable
                                                                                             ){
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryInfo(storageId, packUnitId, pageable)));
    }




}
