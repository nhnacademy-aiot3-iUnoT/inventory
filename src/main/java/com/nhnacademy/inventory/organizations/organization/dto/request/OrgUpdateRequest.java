package com.nhnacademy.inventory.organizations.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrgUpdateRequest(
        @NotBlank(message = "도로명 주소는 필수 입력입니다.")
        @Size(max = 100, message = "도로명 주소는 최대 100자 이내로 입력해야 합니다.")
        String roadAddress,
        @NotBlank(message = "우편번호는 필수 입력입니다.")
        @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다.")
        String zipCode,
        @Size(max = 50, message = "상세주소는 50자 이내로 입력해야 합니다.")
        String addressDetail,
        @Size(max = 255, message = "상세 설명은 255자 이내로 입력해야 합니다.")
        String description
){
}
