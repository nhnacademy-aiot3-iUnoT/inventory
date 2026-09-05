package com.nhnacademy.inventory.organizations.notification.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.notification.dto.request.DepartmentTelegramChatRegisterRequest;
import com.nhnacademy.inventory.organizations.notification.dto.response.DepartmentTelegramChatResponse;
import com.nhnacademy.inventory.organizations.notification.service.DepartmentTelegramChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/organizations/{organization-id}/departments/{department-id}/telegram-chat")
@RequiredArgsConstructor
public class DepartmentTelegramChatController {

    private final DepartmentTelegramChatService departmentTelegramChatService;

    // 부서 단톡방 연결 조회
    @GetMapping
    public ResponseEntity<ApiResponse<DepartmentTelegramChatResponse>> getTelegramChat(
            @PathVariable(name = "organization-id") Long organizationId,
            @PathVariable(name = "department-id") Long departmentId
    ) {
        DepartmentTelegramChatResponse response =
                departmentTelegramChatService.getByDepartment(organizationId, departmentId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 부서 단톡방 연결 및 교체
    @PutMapping
    public ResponseEntity<ApiResponse<DepartmentTelegramChatResponse>> registerTelegramChat(
            @PathVariable(name = "organization-id") Long organizationId,
            @PathVariable(name = "department-id") Long departmentId,
            @RequestBody @Valid DepartmentTelegramChatRegisterRequest request
    ) {
        DepartmentTelegramChatResponse response =
                departmentTelegramChatService.register(organizationId, departmentId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 부서 단톡방 연결 해제
    @DeleteMapping
    public ResponseEntity<Void> unlinkTelegramChat(
            @PathVariable(name = "organization-id") Long organizationId,
            @PathVariable(name = "department-id") Long departmentId
    ) {
        departmentTelegramChatService.unlink(organizationId, departmentId);

        return ResponseEntity.noContent().build();
    }
}
