package com.nhnacademy.inventory.inventories.inventory.operation.inbound.controller;

import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.ExistingLotExpirationResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.service.InboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/inventories")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @GetMapping("/inbound/lot-expiration")
    public ResponseEntity<ApiResponse<ExistingLotExpirationResponse>>
    getExistingLotExpiration(
            @RequestParam("medicine-package-unit-id")
            Long medicinePackageUnitId,
            @RequestParam("zone-id") Long zoneId,
            @RequestParam("lot-number") String lotNumber
    ) {
        ExistingLotExpirationResponse response =
                inboundService.getExistingLotExpiration(
                        medicinePackageUnitId,
                        zoneId,
                        lotNumber
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    // 입고 등록 - 환경기준 등록
    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody MedicineInboundRequest request){

        inboundService.createInbound(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
        // 기존 입고 수량 증가 포함한다는 의미라면 200 ok
    }





}
