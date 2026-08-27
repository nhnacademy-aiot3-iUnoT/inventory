package com.nhnacademy.inventory.organizations.member.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.organizations.member.dto.request.MemberByEmailRequest;
import com.nhnacademy.inventory.organizations.member.dto.request.OrganizationMemberSearchRequest;
import com.nhnacademy.inventory.organizations.member.dto.request.OrganizationRoleUpdateRequest;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberResponse;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberRoleResponse;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/core/members")
@RequiredArgsConstructor
public class OrganizationMemberController {

    private final OrganizationMemberService organizationMemberService;

    /**
     * 부서 지정 조직원 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrganizationMemberResponse>>> getMembers(
            @ModelAttribute OrganizationMemberSearchRequest request,
            @PageableDefault(sort = "joinedAt") Pageable pageable
        ) {
        Page<OrganizationMemberResponse> members = organizationMemberService.findMembers(request, true, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(members)));
    }

    /**
     * 부서 미지정 조직원 목록 조회
     */
    @GetMapping("/without-department")
    public ResponseEntity<ApiResponse<PageResponse<OrganizationMemberResponse>>> getMembersWithoutDepartment(
            @ModelAttribute OrganizationMemberSearchRequest request,
            @PageableDefault(sort = "joinedAt") Pageable pageable
    ) {
        Page<OrganizationMemberResponse> members = organizationMemberService.findMembers(request, false, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(members)));
    }

    /**
     * 조직원 이메일로 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> searchMembers(@Valid @ModelAttribute MemberByEmailRequest request) {
        List<OrganizationMemberResponse> members = organizationMemberService.findAllMembers(request);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    /**
     * 조직원 Role
     */
    @GetMapping("/me/role")
    public ResponseEntity<ApiResponse<OrganizationMemberRoleResponse>> getRole(){
        OrganizationMemberRoleResponse response = organizationMemberService.getRole();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 조직원 역할 수정
     */
    @PutMapping("/{member-id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable("member-id") Long memberId,
                                           @RequestBody @Valid OrganizationRoleUpdateRequest request) {
        organizationMemberService.updateRole(memberId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 조직원 삭제 (BOSS)
     */
    @DeleteMapping("/{member-id}")
    public ResponseEntity<Void> deleteMember(@PathVariable("member-id") Long memberId) {
        organizationMemberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 조직원 탈퇴
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveOrganization() {
        organizationMemberService.leaveOrganization();
        return ResponseEntity.noContent().build();
    }
}
