package com.nhnacademy.inventory.inventories.inventory.controller;


import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.service.InboundService;
import com.nhnacademy.inventory.inventories.inventory.service.InventoriesSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/core/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InboundService inboundService;
    private final InventoriesSearchService inventoriesSearchService;


    // 입고 등록 - 환경기준 등록
    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody MedicineInboundRequest request){

        inboundService.createInbound(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
        // 기존 입고 수량 증가 포함한다는 의미라면 200 ok
    }


    // 전체 입고 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoriesResponse>>> getInventories(@RequestParam(name= "search",required = false) String search,
                                                                                         @RequestParam(name= "storage-id",required = false) Long storageId,
                                                                                         Pageable pageable){

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(inventoriesSearchService.getInventories(search,storageId,pageable))));

    }





}
