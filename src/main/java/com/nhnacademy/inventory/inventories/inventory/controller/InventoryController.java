package com.nhnacademy.inventory.inventories.inventory.controller;


import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryInfoResponse;
import com.nhnacademy.inventory.inventories.inventory.service.InventoriesSearchService;
import com.nhnacademy.inventory.inventories.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/core/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoriesSearchService inventoriesSearchService;
    private final InventoryService inventoryService;



    // 전체 재고 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoriesResponse>>> getInventories(@RequestParam(name= "search",required = false) String search,
                                                                                         @RequestParam(name= "storage-id",required = false) Long storageId,
                                                                                         Pageable pageable){

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(inventoriesSearchService.getInventories(search,storageId,pageable))));

    }

    // 상세 재고 조회
    @GetMapping("/storages/{storage-id}/pack-units/{medicine-package-unit-id}")
    public ResponseEntity<ApiResponse<InventoryInfoResponse>> getInventoryInfo(@PathVariable(name = "storage-id")Long storageId,
                                                                                             @PathVariable(name = "medicine-package-unit-id")Long packUnitId,
                                                                                             Pageable pageable
                                                                                             ){
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryInfo(storageId, packUnitId, pageable)));
    }







    


}
