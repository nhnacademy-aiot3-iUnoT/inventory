package com.nhnacademy.inventory.inventories.inventory.operation.disposal.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalTargetResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.service.MedicineDisposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/inventories")
public class MedicineDisposalController {

    private final MedicineDisposalService medicineDisposalService;

    @GetMapping("/{inventory-id}/disposal-target")
    public ResponseEntity<ApiResponse<MedicineDisposalTargetResponse>>
    getDisposalTarget(
            @PathVariable(name = "inventory-id") Long inventoryId
    ) {
        MedicineDisposalTargetResponse response =
                medicineDisposalService.getDisposalTarget(inventoryId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{inventory-id}/disposal")
    public ResponseEntity<Void> dispose(
            @PathVariable(name = "inventory-id") Long inventoryId,
            @Valid @RequestBody MedicineDisposalRequest request
    ) {
        medicineDisposalService.dispose(inventoryId, request);

        return ResponseEntity.noContent().build();
    }
}
