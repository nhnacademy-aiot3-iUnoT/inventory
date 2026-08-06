package com.nhnacademy.inventory.inventories.alert.dto;

import com.nhnacademy.inventory.inventories.alert.domain.Alert;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;

import java.time.LocalDateTime;

public record AlertInfoResponse(
        Long alertId,
        Long organizationId,
        AlertType alertType,
        String message,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static AlertInfoResponse from(Alert alert){
        return new AlertInfoResponse(
                alert.getId(),
                alert.getOrganization().getId(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getIsRead(),
                alert.getCreatedAt()
        );
    }
}
