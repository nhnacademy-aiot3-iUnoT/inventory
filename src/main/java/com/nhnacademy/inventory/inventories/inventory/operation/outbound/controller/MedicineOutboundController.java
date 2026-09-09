package com.nhnacademy.inventory.inventories.inventory.operation.outbound.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundTargetResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.service.MedicineOutboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/inventories")
@RequiredArgsConstructor
public class MedicineOutboundController {

    private final MedicineOutboundService medicineOutboundService;

    @GetMapping("/{inventory-id}/outbound-target")
    public ResponseEntity<ApiResponse<MedicineOutboundTargetResponse>>
    getOutboundTarget(
            @PathVariable(name = "inventory-id")Long inventoryId
    ) {
        MedicineOutboundTargetResponse response =
                medicineOutboundService.getOutboundTarget(inventoryId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{inventory-id}/outbound")
    public ResponseEntity<Void> outbound(
            @PathVariable(name = "inventory-id") Long inventoryId,
            @Valid @RequestBody MedicineOutboundRequest request
    ) {
        medicineOutboundService.outbound(inventoryId, request);

        return ResponseEntity.noContent().build();
    }
}
