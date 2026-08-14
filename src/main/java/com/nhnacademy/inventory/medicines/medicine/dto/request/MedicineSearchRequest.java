package com.nhnacademy.inventory.medicines.medicine.dto.request;

import com.nhnacademy.inventory.medicines.medicine.domain.SearchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicineSearchRequest(

        @NotNull(message = "검색 조건은 필수입니다.")
        SearchType searchType,
        @NotBlank(message = "검색어를 입력하세요.")
        String search
) {
}
