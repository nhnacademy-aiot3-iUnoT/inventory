package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.department.dto.request.MemberDepartmentAssignRequest;
import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class MemberDepartmentController {

    private final MemberDepartmentService memberDepartmentService;

    /**
     * 부서 아이디 -> 조직원 정보
     */
    @GetMapping("/departments/{department-id}/members")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getDepartmentMembers(@PathVariable("department-id") Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(memberDepartmentService.getDepartmentMembers(departmentId)));
    }

    /**
     * 부서에 조직원 추가
     */
    @PostMapping("/departments/{department-id}/members/{member-id}")
    public ResponseEntity<Void> addMember(@PathVariable("department-id") Long departmentId,
                                          @PathVariable("member-id") Long memberId) {
        memberDepartmentService.addMember(departmentId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 부서에서 조직원 삭제
     */
    @DeleteMapping("/departments/{department-id}/members/{member-id}")
    public ResponseEntity<Void> removeMember(@PathVariable("department-id") Long departmentId,
                                             @PathVariable("member-id") Long memberId) {
        memberDepartmentService.removeMember(departmentId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 조직원 부서 지정 (한번에 여러개)
     */
    @PostMapping("/members/{member-id}/departments")
    public ResponseEntity<Void> assignMemberDepartments(@PathVariable("member-id") Long memberId,
                                                        @RequestBody MemberDepartmentAssignRequest request) {
        memberDepartmentService.assignMemberDepartments(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
