package com.nhnacademy.inventory.organizations.invitation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LeaveOrgRequest(
    @NotNull(message = "사용자 UUID는 필수 입니다")
    UUID accountUuid,

    @Email(message = "올바른 이메일 형식이 아닙니다")
    String previousEmail // 기존 초대 내역 정리용, 선택
) {
}
