package com.nhnacademy.inventory.organizations.member.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.request.LeaveOrgRequest;
import com.nhnacademy.inventory.organizations.member.dto.response.MemberOrganizationResponse;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// API 서버 간 통신
@RestController
@RequestMapping("/api/core/internal")
@RequiredArgsConstructor
public class OrganizationMemberInternalController {

    private final OrganizationMemberService organizationMemberService;

    /**
     * Rule-Engine -> accountUuid로 소속 조직 ID와 조직 역할 조회
     */
    @GetMapping("/members/{account-uuid}/organization")
    public ResponseEntity<ApiResponse<MemberOrganizationResponse>> getMemberOrganization(
            @PathVariable(name = "account-uuid") UUID accountUuid
    ) {
        MemberOrganizationResponse response = organizationMemberService.getMemberOrganization(accountUuid);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Account 탈퇴 연동 API
     */
    @PostMapping("/leave-org")
    public ResponseEntity<Void> leaveOrgForAccountLeave(@Valid @RequestBody LeaveOrgRequest request) {
        organizationMemberService.leaveOrgForAccountLeave(request);
        return ResponseEntity.noContent().build();
    }
}
