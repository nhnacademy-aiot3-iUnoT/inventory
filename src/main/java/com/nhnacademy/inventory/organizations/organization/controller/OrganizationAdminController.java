package com.nhnacademy.inventory.organizations.organization.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgCreateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgSearchRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.AdminOrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgCreateResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgSearchResponse;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/admin/organizations")
@RequiredArgsConstructor
public class OrganizationAdminController {
    private final OrganizationService organizationService;

    /**
     * 조직 생성 (사업자 번호, 조직명, 이메일(Owner))
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrgCreateResponse>> createOrganization(@RequestBody @Valid OrgCreateRequest request) {
        OrgCreateResponse response = organizationService.createOrganization(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 조직 목록 조회 (status, name)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrgSearchResponse>>> getOrganizationList(
            @RequestBody @Valid OrgSearchRequest request,
            @PageableDefault(size=15, sort = "createdAt") Pageable pageable
    ) {
        Page<OrgSearchResponse> organizationList = organizationService.getOrganizationList(request, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(organizationList)));
    }

    /**
     * 조직 상세 조회
     */
    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<AdminOrgDetailResponse>> getOrganizationInfo(@PathVariable Long organizationId) {
        AdminOrgDetailResponse organizationForAdmin = organizationService.getOrganizationForAdmin(organizationId);

        return ResponseEntity.ok(ApiResponse.success(organizationForAdmin));
    }

    /**
     * 조직 삭제 처리
     */
    @DeleteMapping("/{organizationId}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long organizationId) {
        organizationService.deleteOrganization(organizationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
