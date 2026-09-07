package com.nhnacademy.inventory.organizations.notification.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.notification.dto.response.NotificationChannelPreferenceResponse;
import com.nhnacademy.inventory.organizations.notification.dto.response.NotificationScopePreferenceEffectiveResponse;
import com.nhnacademy.inventory.organizations.notification.dto.response.NotificationScopePreferenceResponse;
import com.nhnacademy.inventory.organizations.notification.dto.request.NotificationChannelPreferencesUpdateRequest;
import com.nhnacademy.inventory.organizations.notification.dto.request.NotificationScopePreferenceRequest;
import com.nhnacademy.inventory.organizations.notification.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/organizations/{organization-id}/notifications/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {
    private final NotificationPreferenceService notificationPreferenceService;

    // 저장소/구역 on/off 버튼
    @PutMapping("/scopes")
    public ResponseEntity<Void> upsertScopePreference(
            @PathVariable(name = "organization-id") Long organizationId,
            @RequestBody @Valid NotificationScopePreferenceRequest request
    ) {
        notificationPreferenceService.upsertScopePreference(organizationId, request);

        return ResponseEntity.noContent().build();
    }

    // 일괄 관리 페이지에서 직접 설정 목록 조회
    @GetMapping("/scopes")
    public ResponseEntity<ApiResponse<List<NotificationScopePreferenceResponse>>> getScopePreferences(
            @PathVariable(name = "organization-id") Long organizationId
    ) {
        List<NotificationScopePreferenceResponse> response =
                notificationPreferenceService.getScopePreferences(organizationId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 저장소/구역 버튼 현재 상태 조회
    @GetMapping("/scopes/effective")
    public ResponseEntity<ApiResponse<NotificationScopePreferenceEffectiveResponse>> getEffectiveScopePreference(
            @PathVariable(name = "organization-id") Long organizationId,
            @RequestParam(name = "storage-id", required = false) Long storageId,
            @RequestParam(name = "zone-id", required = false) Long zoneId
    ) {
        NotificationScopePreferenceEffectiveResponse response =
                notificationPreferenceService.getEffectiveScopePreference(
                        organizationId,
                        storageId,
                        zoneId
                );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 직접 설정 삭제, 즉 상위 설정 따르기로 되돌리기
    @DeleteMapping("/scopes")
    public ResponseEntity<Void> deleteScopePreference(
            @PathVariable(name = "organization-id") Long organizationId,
            @RequestParam(name = "storage-id", required = false) Long storageId,
            @RequestParam(name = "zone-id", required = false) Long zoneId
    ) {
        notificationPreferenceService.deleteScopePreference(
                organizationId,
                storageId,
                zoneId
        );

        return ResponseEntity.noContent().build();
    }

    // 개인정보 알림 채널 조회
    @GetMapping("/channels")
    public ResponseEntity<ApiResponse<List<NotificationChannelPreferenceResponse>>> getChannelPreferences(
            @PathVariable(name = "organization-id") Long organizationId
    ) {
        List<NotificationChannelPreferenceResponse> response =
                notificationPreferenceService.getChannelPreferences(organizationId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 개인정보 알림 채널 저장
    @PutMapping("/channels")
    public ResponseEntity<Void> updateChannelPreferences(
            @PathVariable(name = "organization-id") Long organizationId,
            @RequestBody @Valid NotificationChannelPreferencesUpdateRequest request
    ) {
        notificationPreferenceService.updateChannelPreferences(organizationId, request);

        return ResponseEntity.noContent().build();
    }


}
