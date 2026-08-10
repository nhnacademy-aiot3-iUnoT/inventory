package com.nhnacademy.inventory.organizations.organization.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrgCreateRequest(
        @NotBlank(message = "사업자 번호는 필수 입력입니다.")
        @Pattern(
                regexp = "\\d{10}",
                message = "사업자 번호는 숫자 10자리입니다."
        )
        String businessNumber,

        @NotBlank(message = "이메일은 필수 입력입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 255자 이내로 입력해야 합니다.")
        String email,

        @NotBlank(message = "조직명은 필수 입력입니다.")
        @Size(max = 50, message = "조직명은 50자 이내로 입력해야 합니다.")
        String name
){
}
