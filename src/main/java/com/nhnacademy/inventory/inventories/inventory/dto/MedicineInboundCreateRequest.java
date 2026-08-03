package com.nhnacademy.inventory.inventories.inventory.dto;

import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentTypeRequest;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MedicineInboundCreateRequest(

        @NotNull(message = "의약품을 선택해주세요.")
        Long medicinePackageUnitId,
        @NotNull(message = "보관 구역을 선택해주세요.")
        Long zoneId,
        @NotBlank(message = "제조번호를 입력해주세요.")
        @Size(max=50, message = "제조번호는 50자 이하여야 합니다.")
        String lotNumber,
        @NotNull(message = "유통기한을 입력해주세요.")
        @Future(message = "실제 유통기한은 현재 날짜 이후여야 합니다.")
        LocalDate expirationDate,
        @NotNull(message = "입고 수량을 입력해주세요.")
        @Positive(message = "수량은 양수여야 합니다.")
        Integer quantity,

        MedicineEnvironmentTypeRequest environmentStandardRequest

) {
}
