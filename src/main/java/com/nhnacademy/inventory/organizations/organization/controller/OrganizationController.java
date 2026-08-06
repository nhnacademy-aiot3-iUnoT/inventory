package com.nhnacademy.inventory.organizations.organization.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrganizationSetupRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    /**
     * 조직 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OrgDetailResponse>> getOrganizationInfo(){
        OrgDetailResponse organizationForUser = organizationService.getOrganizationForUser();
        return ResponseEntity.ok(ApiResponse.success(organizationForUser));
    }

    /**
     * 조직 상태 활성화 <-> 비활성화 (OWNER 체크)
     */
    @PutMapping("/me/status")
    public ResponseEntity<Void> updateOrganizationStatus(@RequestBody @Valid OrgStatusUpdateRequest request) {
        organizationService.updateOrganizationStatus(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 조직 정보 수정 (주소, 상세설명)
     */
    @PutMapping("/me")
    public ResponseEntity<Void> updateOrganization(@RequestBody @Valid OrgUpdateRequest request) {
        organizationService.updateOrganization(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 조직 초기화 (admin이 조직 생성 후 owner 최초 입력)
     */
    @PutMapping("/me/setup")
    public ResponseEntity<Void> setupOrganization(@RequestBody @Valid OrganizationSetupRequest request) {
        organizationService.setupOrganization(request);
        return ResponseEntity.noContent().build();
    }

}
