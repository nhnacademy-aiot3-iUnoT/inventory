package com.nhnacademy.inventory.inventories.alert.dto;

import com.nhnacademy.inventory.inventories.alert.domain.Alert;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record AlertInfoResponse(
        Long alertId,
        Long organizationId,
        String organizationName,
        AlertType alertType,
        String message,
        Boolean isChecked,
        LocalDateTime createdAt
) {
    public static AlertInfoResponse from(Alert alert){
        return new AlertInfoResponse(
                alert.getId(),
                alert.getOrganization().getId(),
                alert.getOrganization().getName(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getIsChecked(),
                alert.getCreatedAt()
        );
    }

    @QueryProjection
    public AlertInfoResponse{

    }
}
