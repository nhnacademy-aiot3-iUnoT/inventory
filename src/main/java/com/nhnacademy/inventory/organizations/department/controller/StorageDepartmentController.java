package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.organizations.department.service.StorageDepartmentService;
import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.StorageByDepartmentResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core")
public class StorageDepartmentController {
    private final StorageDepartmentService storageDepartmentService;

    /**
     * 부서 아이디 -> 저장소 목록
     */
    @GetMapping("/departments/{department-id}/storages")
    public ResponseEntity<ApiResponse<List<StorageByDepartmentResponse>>> getStoragesByDepartmentId(@PathVariable("department-id") Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(storageDepartmentService.getStoragesByDepartmentId(departmentId)));
    }

    /**
     * 저장소 아이디 -> 부서 목록
     */
    @GetMapping("/storages/{storage-id}/departments")
    public ResponseEntity<ApiResponse<List<DepartmentByStorageResponse>>> getDepartmentsByStorageId(@PathVariable("storage-id") Long storageId) {
        return ResponseEntity.ok(ApiResponse.success(storageDepartmentService.getDepartmentsByStorageId(storageId)));
    }

    /**
     * 부서 저장소 연결
     */
    @PostMapping("/departments/{department-id}/storages/{storage-id}")
    public ResponseEntity<Void> addStorage(@PathVariable("department-id") Long departmentId,
                                           @PathVariable("storage-id") Long storageId) {
        storageDepartmentService.addStorage(departmentId, storageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 부서 저장소 연결 해제
     */
    @DeleteMapping("/departments/{department-id}/storages/{storage-id}")
    public ResponseEntity<Void> removeStorage(@PathVariable("department-id") Long departmentId,
                                              @PathVariable("storage-id") Long storageId) {
        storageDepartmentService.removeStorage(departmentId, storageId);
        return ResponseEntity.noContent().build();
    }
}
