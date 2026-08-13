package com.nhnacademy.inventory.organizations.invitation.controller;

import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 시스템 관리자 (admin) Owner 초대 관리
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/admin/organizations")
public class AdminInvitationController {
    private final InvitationService invitationService;

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
