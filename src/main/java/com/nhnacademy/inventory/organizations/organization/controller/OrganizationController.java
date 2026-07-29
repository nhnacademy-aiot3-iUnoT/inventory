package com.nhnacademy.inventory.organizations.organization.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/core/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    /**
     * 소속된 조직 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OrgDetailResponse>> getOrganizationInfo(){
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        OrgDetailResponse organizationForUser = organizationService.getOrganizationForUser(uuid);
        return ResponseEntity.ok(ApiResponse.success(organizationForUser));
    }

    /**
     * 조직 상태 활성화 <-> 비활성화 (OWNER 체크)
     */
    @PutMapping("/me/status")
    public ResponseEntity<Void> updateOrganizationStatus(@RequestBody @Valid OrgStatusUpdateRequest request) {
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        organizationService.updateOrganizationStatus(uuid, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 조직 정보 수정 (주소, 상세설명)
     */
    @PutMapping("/me")
    public ResponseEntity<Void> updateOrganization(@RequestBody @Valid OrgUpdateRequest request) {
        UUID uuid = new UUID(0L, 0L); // 임시 uuid

        organizationService.updateOrganization(uuid, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
