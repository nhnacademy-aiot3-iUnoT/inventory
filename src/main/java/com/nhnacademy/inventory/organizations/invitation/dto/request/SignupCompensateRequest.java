package com.nhnacademy.inventory.organizations.invitation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SignupCompensateRequest (
        @NotNull(message = "초대 토큰은 필수입니다.")
        UUID token,
        @NotNull(message = "회원 아이디는 필수입니다.")
        UUID accountUuid
){
}
