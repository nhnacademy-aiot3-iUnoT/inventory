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
@RequestMapping("/api/core/departments/{department-id}/telegram-chat")
@RequiredArgsConstructor
public class DepartmentTelegramChatController {

    private final DepartmentTelegramChatService departmentTelegramChatService;

    // 부서 단톡방 연결 조회. 연결이 없으면 data가 null이다.
    @GetMapping
    public ResponseEntity<ApiResponse<DepartmentTelegramChatResponse>> getTelegramChat(
            @PathVariable(name = "department-id") Long departmentId
    ) {
        DepartmentTelegramChatResponse response =
                departmentTelegramChatService.getByDepartment(departmentId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 부서 단톡방 연결 및 교체
    @PutMapping
    public ResponseEntity<ApiResponse<DepartmentTelegramChatResponse>> registerTelegramChat(
            @PathVariable(name = "department-id") Long departmentId,
            @RequestBody @Valid DepartmentTelegramChatRegisterRequest request
    ) {
        DepartmentTelegramChatResponse response =
                departmentTelegramChatService.register(departmentId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 부서 단톡방 연결 해제
    @DeleteMapping
    public ResponseEntity<Void> unlinkTelegramChat(
            @PathVariable(name = "department-id") Long departmentId
    ) {
        departmentTelegramChatService.unlink(departmentId);

        return ResponseEntity.noContent().build();
    }
}
