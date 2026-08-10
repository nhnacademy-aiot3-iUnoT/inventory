package com.nhnacademy.inventory.organizations.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrganizationSetupRequest(

        @NotBlank(message = "우편번호는 필수입니다.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 숫자 5자리입니다.")
        String zipCode,

        @NotBlank(message = "도로명 주소는 필수입니다.")
        @Size(max = 255, message = "도로명 주소는 255자 이내입니다.")
        String roadAddress,

        @Size(max = 255, message = "상세 주소는 255자 이내입니다.")
        String addressDetail,

        @Size(max = 255, message = "조직 소개는 255자 이내입니다.")
        String description

) {
}
