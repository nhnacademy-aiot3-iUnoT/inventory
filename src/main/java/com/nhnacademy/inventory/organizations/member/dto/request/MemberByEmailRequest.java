package com.nhnacademy.inventory.organizations.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberByEmailRequest(
        @NotBlank(message = "검색할 조직원의 이메일을 입력하세요.")
        String email
) {
}
