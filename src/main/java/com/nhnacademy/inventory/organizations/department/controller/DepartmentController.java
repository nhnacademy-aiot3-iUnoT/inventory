package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentCreateRequest;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentCreateResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentInfoResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.service.DepartmentService;
import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    private final MemberDepartmentService memberDepartmentService;

    /**
     * 부서 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentCreateResponse>> createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentCreateResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 전체 부서 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentListResponse>>> getDepartments() {
        List<DepartmentListResponse> responses = departmentService.getDepartments();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 본인 부서 목록 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<DepartmentListResponse>>> getMyDepartments() {
        List<DepartmentListResponse> responses = memberDepartmentService.getMyDepartments();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 부서 단건 조회
     */
    @GetMapping("/{department-id}")
    public ResponseEntity<ApiResponse<DepartmentInfoResponse>> getDepartment(@PathVariable(name = "department-id") Long departmentId) {
        DepartmentInfoResponse response = departmentService.getDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 부서 활성화 <-> 비활성화
     */
    @PutMapping("/{department-id}/status")
    public ResponseEntity<Void> updateDepartmentStatus(
            @Valid @RequestBody DepartmentStatusUpdateRequest request,
            @PathVariable(name = "department-id") Long departmentId
    ) {
        departmentService.updateDepartmentStatus(request, departmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 부서 정보 수정
     */
    @PutMapping("/{department-id}")
    public ResponseEntity<Void> updateDepartment(
            @Valid @RequestBody DepartmentUpdateRequest request,
            @PathVariable(name = "department-id") Long departmentId
    ) {
        departmentService.updateDepartment(request, departmentId);
        return ResponseEntity.noContent().build();

    }

    /**
     * 부서 삭제
     */
    @DeleteMapping("/{department-id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable(name = "department-id") Long departmentId) {
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }
}
