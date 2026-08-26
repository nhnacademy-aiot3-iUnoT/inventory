package com.nhnacademy.inventory.organizations.member.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.member.dto.response.MemberOrganizationResponse;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// API 서버 간 통신
@RestController
@RequestMapping("/api/core/internal/members")
@RequiredArgsConstructor
public class OrganizationMemberInternalController {

    private final OrganizationMemberService organizationMemberService;

    /**
     * Rule-Engine -> accountUuid로 소속 조직 ID와 조직 역할 조회
     */
    @GetMapping("/{account-uuid}/organization")
    public ResponseEntity<ApiResponse<MemberOrganizationResponse>> getMemberOrganization(
            @PathVariable(name = "account-uuid") UUID accountUuid
    ) {
        MemberOrganizationResponse response = organizationMemberService.getMemberOrganization(accountUuid);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
