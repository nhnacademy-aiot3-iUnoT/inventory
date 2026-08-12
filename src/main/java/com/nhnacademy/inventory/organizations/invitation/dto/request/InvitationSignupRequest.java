package com.nhnacademy.inventory.organizations.invitation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InvitationSignupRequest (
        @NotNull(message = "초대 토큰은 필수입니다.")
        UUID token,
        @NotNull(message = "초대 이메일은 필수입니다.")
        String email,
        @NotNull(message = "회원 아이디는 필수입니다.")
        UUID accountUuid
){
}
