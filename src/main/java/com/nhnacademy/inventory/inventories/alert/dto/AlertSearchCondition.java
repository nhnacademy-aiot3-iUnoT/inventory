package com.nhnacademy.inventory.inventories.alert.dto;

import com.nhnacademy.inventory.inventories.alert.domain.AlertType;

public record AlertSearchCondition (
        AlertType alertType,
        Boolean isRead
){
}
