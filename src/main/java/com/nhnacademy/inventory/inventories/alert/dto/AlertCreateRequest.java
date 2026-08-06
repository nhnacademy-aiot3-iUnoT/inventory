package com.nhnacademy.inventory.inventories.alert.dto;

import com.nhnacademy.inventory.inventories.alert.domain.AlertType;

public record AlertCreateRequest(
        Long organizationId,
        AlertType alertType,
        String message
) {
}
