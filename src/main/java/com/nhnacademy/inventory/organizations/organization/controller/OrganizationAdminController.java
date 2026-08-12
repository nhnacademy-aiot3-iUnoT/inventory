package com.nhnacademy.inventory.organizations.organization.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationCreateResponse;
import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgCreateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgSearchRequest;
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

/**
 * 시스템 관리자 (admin) 조직 관리
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/admin/organizations")
public class OrganizationAdminController {
    private final OrganizationService organizationService;
    private final InvitationService invitationService;

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
            @ModelAttribute OrgSearchRequest request,
            @PageableDefault(sort = "createdAt") Pageable pageable
    ) {
        Page<OrgSearchResponse> organizationList = organizationService.getOrganizationList(request, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(organizationList)));
    }

    /**
     * 조직 상세 조회
     */
    @GetMapping("/{organization-id}")
    public ResponseEntity<ApiResponse<AdminOrgDetailResponse>> getOrganizationInfo(@PathVariable(name = "organization-id") Long organizationId) {
        AdminOrgDetailResponse organizationForAdmin = organizationService.getOrganizationForAdmin(organizationId);

        return ResponseEntity.ok(ApiResponse.success(organizationForAdmin));
    }

    /**
     * 조직 삭제 처리
     */
    @DeleteMapping("/{organization-id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable(name = "organization-id") Long organizationId) {
        organizationService.deleteOrganization(organizationId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 재전송
     */
    @PostMapping("/{organization-id}/invitations/{invitation-id}/resend")
    public ResponseEntity<Void> resendInvitation(@PathVariable(name = "organization-id") Long organizationId,
                                                 @PathVariable(name = "invitation-id") Long invitationId) {
        invitationService.resendInvitationForAdmin(organizationId, invitationId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 취소
     */
    @DeleteMapping("/{organization-id}/invitations/{invitation-id}")
    public ResponseEntity<Void> cancelInvitation(@PathVariable(name = "organization-id") Long organizationId,
                                                 @PathVariable(name = "invitation-id") Long invitationId) {
        invitationService.cancelInvitationForAdmin(organizationId, invitationId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 재발급
     */
    @PostMapping("/{organization-id}/invitations/{invitation-id}/reissue")
    public ResponseEntity<Void> reissueInvitation(@PathVariable(name = "organization-id") Long organizationId, @PathVariable(name = "invitation-id") Long invitationId) {
        invitationService.reissueInvitationForAdmin(organizationId, invitationId);

        return ResponseEntity.noContent().build();
    }
}
