package com.nhnacademy.inventory.organizations.invitation.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationCreateRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.*;
import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.organizations.invitation.service.OrganizationInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core")
public class InvitationController {

    private final InvitationService invitationService;
    private final OrganizationInvitationService organizationInvitationService;

    /**
     * 조직원 초대 생성
     * OWNER 전용
     */
    @PostMapping("/organizations/me/invitations")
    public ResponseEntity<ApiResponse<InvitationCreateResponse>> createInvitation(@Valid @RequestBody InvitationCreateRequest request) {
        InvitationCreateResponse response = organizationInvitationService.inviteMember(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 초대 목록 조회 (owner)
     */
    @GetMapping("/organizations/me/invitations")
    public ResponseEntity<ApiResponse<PageResponse<InvitationSearchResponse>>> getInvitations(
            @ModelAttribute InvitationSearchRequest request,
            @PageableDefault(sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        PageResponse.from(invitationService.getInvitations(request, pageable))
                )
        );
    }

    /**
     * 토큰 검증만 (초대 링크 진입)
     */
    @GetMapping("/invitations/{token}")
    public ResponseEntity<Void> validateInvitationToken(@PathVariable UUID token) {
        invitationService.validateInvitationToken(token);
        return ResponseEntity.noContent().build();
    }

    /**
     * 초대 재전송
     */
    @PostMapping("/organizations/me/invitations/{invitation-id}/resend")
    public ResponseEntity<Void> resendInvitation(@PathVariable(name = "invitation-id") Long invitationId) {
        invitationService.resendInvitation(invitationId);

        return ResponseEntity.noContent().build();
    }


    /**
     * 초대 취소
     */
    @DeleteMapping("/organizations/me/invitations/{invitation-id}")
    public ResponseEntity<Void> cancelInvitation(@PathVariable(name = "invitation-id") Long invitationId) {
        invitationService.cancelInvitation(invitationId);

        return ResponseEntity.noContent().build();
    }


    /**
     * 초대 재발급
     */
    @PostMapping("/organizations/me/invitations/{invitation-id}/reissue")
    public ResponseEntity<ApiResponse<InvitationCreateResponse>> reissueInvitation(@PathVariable(name = "invitation-id") Long invitationId) {
        return ResponseEntity.ok(ApiResponse.success(invitationService.reissueInvitation(invitationId)));
    }
}
