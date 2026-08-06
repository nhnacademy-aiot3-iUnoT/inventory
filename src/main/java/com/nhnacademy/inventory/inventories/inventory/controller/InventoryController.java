package com.nhnacademy.inventory.inventories.inventory.controller;


import com.nhnacademy.inventory.inventories.inventory.dto.MedicineInboundCreateRequest;
import com.nhnacademy.inventory.inventories.inventory.service.MedicineInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class InventoryController {

    private final MedicineInventoryService medicineInventoryService;


    // 입고 등록
    @PostMapping("/medicine-inventories")
    public ResponseEntity<Void> register(@Valid @RequestBody MedicineInboundCreateRequest inboundRequest){

        medicineInventoryService.inbound(inboundRequest);
        return ResponseEntity.ok().build();
        // 기존 입고 수량 증가 포함한다는 의미라면 200 ok
    }




}
