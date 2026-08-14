package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/members/{member-id}/departments")
@RequiredArgsConstructor
public class MemberDepartmentController {

    private final MemberDepartmentService memberDepartmentService;

    /**
     * 조직원에게 부서 지정
     * memberId = 조직원 아이디
     */
    @PostMapping("/{department-id}")
    public ResponseEntity<Void> assignDepartment(@PathVariable("member-id") Long memberId,
                                                 @PathVariable("department-id") Long departmentId) {
        memberDepartmentService.assignDepartment(memberId, departmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 조직원에게서 부서 지정 해제
     */
    @DeleteMapping("/{department-id}")
    public ResponseEntity<Void> removeDepartment(@PathVariable("member-id") Long memberId,
                                                 @PathVariable("department-id") Long departmentId) {
        memberDepartmentService.removeDepartment(memberId, departmentId);
        return ResponseEntity.noContent().build();
    }
}
