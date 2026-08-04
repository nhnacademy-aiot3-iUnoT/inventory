package com.nhnacademy.inventory.medicines.medicine.repository;

import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MedicinePackageUnitRepositoryCustom {

    Page<MedicinePackageSearchResponse> findAllWithMedicineByProductName(String productName, Pageable pageable);

    Page<MedicinePackageSearchResponse> findAllWithMedicineByItemCode(String itemCode, Pageable pageable);

    Optional<MedicinePackageDetailResponse> findDetailMedicine(Long packUnitId);

    // Optional로 감싸는 이유
    // 해당 Id의 데이터가 없을 수도 있기 때문
}
