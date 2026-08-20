package com.nhnacademy.inventory.inventories.inventory.operation.outbound.controller;

import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.service.MedicineOutboundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class MedicineOutboundController {

    private final MedicineOutboundService medicineOutboundService;

    @PostMapping("/medicine-inventories/outbound")
    public ResponseEntity<Void> outbound(
            @Valid @RequestBody MedicineOutboundRequest request,
            @AuthenticationPrincipal Jwt jwt
            ) {

        UUID processedBy = UUID.fromString(jwt.getSubject());
        medicineOutboundService.outbound(request,processedBy);

        return ResponseEntity.noContent().build();
    }

}
