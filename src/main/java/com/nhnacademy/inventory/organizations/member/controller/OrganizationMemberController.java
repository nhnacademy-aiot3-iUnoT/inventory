package com.nhnacademy.inventory.organizations.member.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberListResponse;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/members")
@RequiredArgsConstructor
public class OrganizationMemberController {

    private final OrganizationMemberService organizationMemberService;

    /**
     * 조직원 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationMemberListResponse>>> getMembers() {
        List<OrganizationMemberListResponse> members = organizationMemberService.getMembers();
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    /**
     * 부서 미지정 조직원 목록 조회
     */
    @GetMapping("/without-department")
    public ResponseEntity<ApiResponse<List<OrganizationMemberListResponse>>> getMembersWithoutDepartment() {
        List<OrganizationMemberListResponse> members = organizationMemberService.getMembersWithoutDepartment();
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    /**
     * 조직원 단건 조회
     */
//    @GetMapping("/{member-id}")
//    public ResponseEntity<ApiResponse<OrganizationMemberInfoResponse>> getMember(@PathVariable("member-id") Long memberId) {
//        OrganizationMemberInfoResponse member = organizationMemberService.getMember(memberId);
//        return ResponseEntity.ok(ApiResponse.success(member));
//    }

    /**
     * 조직원 역할 수정
     */
//    @PutMapping("/{member-id}/role")
//    public ResponseEntity<ApiResponse<OrganizationMemberInfoResponse>> updateRole(
//            @PathVariable("member-id") Long memberId,
//            @RequestBody @Valid OrganizationRoleUpdateRequest request
//    ) {
//        OrganizationMemberInfoResponse member = organizationMemberService.updateRole(memberId, request);
//        return ResponseEntity.ok(ApiResponse.success(member));
//    }

//    /**
//     * 조직원 삭제 (owner)
//     */
//    @DeleteMapping("/{member-id}")
//    public ResponseEntity<Void> deleteMember() {
//        organizationMemberService.deleteMember(memberId);
//        return ResponseEntity.noContent().build();
//    }

//    /**
//     * 조직원 탈퇴
//     */
//    @DeleteMapping("/me")
//    public ResponseEntity<Void> leaveOrganization() {
//        organizationMemberService.leaveOrganization();
//        return ResponseEntity.noContent().build();
//    }
}
