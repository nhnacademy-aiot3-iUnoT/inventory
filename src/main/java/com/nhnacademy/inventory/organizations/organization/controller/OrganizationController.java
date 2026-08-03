package com.nhnacademy.inventory.organizations.organization.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrganizationCompleteRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    /**
     * 소속된 조직 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OrgDetailResponse>> getOrganizationInfo(
            @RequestHeader("X-User-Id") UUID accountUuid
    ){
        OrgDetailResponse organizationForUser = organizationService.getOrganizationForUser(accountUuid);
        return ResponseEntity.ok(ApiResponse.success(organizationForUser));
    }

    /**
     * 조직 상태 활성화 <-> 비활성화 (OWNER 체크)
     */
    @PutMapping("/me/status")
    public ResponseEntity<Void> updateOrganizationStatus(@RequestHeader("X-User-Id") UUID accountUuid,
                                                         @RequestBody @Valid OrgStatusUpdateRequest request) {
        organizationService.updateOrganizationStatus(accountUuid, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 조직 정보 수정 (주소, 상세설명)
     */
    @PutMapping("/me")
    public ResponseEntity<Void> updateOrganization(@RequestHeader("X-User-Id") UUID accountUuid,
                                                   @RequestBody @Valid OrgUpdateRequest request) {
        organizationService.updateOrganization(accountUuid, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 조직 초기화 (admin이 조직 생성 후 owner 최초 입력)
     */
    @PutMapping("/me/complete")
    public ResponseEntity<Void> completeOrganization(@RequestHeader("X-User-Id") UUID accountUuid,
                                                     @RequestBody @Valid OrganizationCompleteRequest request) {
        organizationService.completeOrganization(accountUuid, request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
