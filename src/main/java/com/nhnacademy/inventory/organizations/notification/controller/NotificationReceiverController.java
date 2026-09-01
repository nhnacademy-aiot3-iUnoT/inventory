package com.nhnacademy.inventory.organizations.notification.controller;


import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.organizations.notification.dto.reponse.NotificationPreferenceResponse;
import com.nhnacademy.inventory.organizations.notification.service.NotificationReceiverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/core/internal/notifications")
@RequiredArgsConstructor
public class NotificationReceiverController {

    private final NotificationReceiverService notificationReceiverService;

    // 룰엔진 내부 수신자 조회
    @GetMapping("/receivers")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceResponse>>> getReceiversForRuleEngine(
            @RequestParam("organization-id") Long organizationId,
            @RequestParam("storage-id") Long storageId,
            @RequestParam("zone-id") Long zoneId
    ) {
        List<NotificationPreferenceResponse> response =
                notificationReceiverService.getReceiversForRuleEngine(
                        organizationId,
                        storageId,
                        zoneId
                );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
