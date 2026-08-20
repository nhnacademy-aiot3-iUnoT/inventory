package com.nhnacademy.inventory.inventories.inventory.operation.disposal.controller;

import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.service.MedicineDisposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;


import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core")
public class MedicineDisposalController {

    private final MedicineDisposalService medicineDisposalService;

    @PostMapping("/medicine-inventories/disposal")
    public ResponseEntity<Void> dispose(
            @Valid @RequestBody MedicineDisposalRequest request,
            @AuthenticationPrincipal Jwt jwt
            ) {
        UUID processedBy = UUID.fromString(jwt.getSubject());

        medicineDisposalService.dispose(request,processedBy);

        return ResponseEntity.noContent().build();
    }
}
