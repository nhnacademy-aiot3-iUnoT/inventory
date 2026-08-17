package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.department.dto.request.MemberDepartmentUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/members")
@RequiredArgsConstructor
public class MemberDepartmentController {

    private final MemberDepartmentService memberDepartmentService;

    /**
     * 조직원이 속한 부서
     */
    @GetMapping("/{member-id}/departments")
    public ResponseEntity<ApiResponse<List<DepartmentListResponse>>> getMemberDepartments(@PathVariable("member-id") Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberDepartmentService.getMemberDepartments(memberId)));
    }

    /**
     * 조직원_부서 수정
     */
    @PutMapping("/{member-id}/departments")
    public ResponseEntity<Void> updateMemberDepartments(@PathVariable("member-id") Long memberId,
                                                        @RequestBody MemberDepartmentUpdateRequest request) {
        memberDepartmentService.updateMemberDepartments(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
