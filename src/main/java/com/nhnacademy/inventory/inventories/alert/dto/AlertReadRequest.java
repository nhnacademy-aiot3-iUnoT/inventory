package com.nhnacademy.inventory.inventories.alert.dto;

import java.util.List;

public record AlertReadRequest(
        List<Long> alertIds
) {
}
