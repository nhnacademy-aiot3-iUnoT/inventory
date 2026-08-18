package com.nhnacademy.inventory.global.dto.account;

import java.util.UUID;

public record AccountResponse(
        UUID accountUuid,
        String email
) {
}
