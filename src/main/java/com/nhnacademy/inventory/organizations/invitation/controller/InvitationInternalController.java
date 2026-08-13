package com.nhnacademy.inventory.organizations.invitation.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSignupRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.request.SignupCompensateRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationSignupResponse;
import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// API 서버 간 통신
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/internal/invitations")
public class InvitationInternalController {
    private final InvitationService invitationService;

    /**
     * Account -> 토큰 검증, 이메일 일치 판단, 조직원 생성, 초대 토큰 사용 처리
     */
    @PostMapping("/use")
    public ResponseEntity<ApiResponse<InvitationSignupResponse>> signupWithInvitation(@Valid @RequestBody InvitationSignupRequest request) {
        InvitationSignupResponse response = invitationService.signupWithInvitation(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원가입 실패 보상 처리
     */
    @PostMapping("/compensate")
    public ResponseEntity<Void> compensateSignup(@Valid @RequestBody SignupCompensateRequest request) {
        invitationService.compensateSignup(request);
        return ResponseEntity.noContent().build();
    }
}
