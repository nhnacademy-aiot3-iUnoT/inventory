package com.nhnacademy.inventory.medicines.medicine.controller;


import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.dto.PageResponse;

import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.request.MedicineSearchRequest;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineApiService;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/medicines")
public class MedicineController {

    private final MedicineSearchService medicineSearchService;


//    // 공공 데이터 저장
//    @PostMapping("/admin/import")
//    public ResponseEntity<Void> savedMedicines(){
//
//        medicineApiService.savedAllMedicines();
//        return ResponseEntity.noContent().build();
//
//    }

    // 의약품 정보 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MedicinePackageSearchResponse>>> getMedicines(@Valid @ModelAttribute MedicineSearchRequest medicineSearchRequest, Pageable pageable){

        Page<MedicinePackageSearchResponse> page = medicineSearchService.getMedicines(medicineSearchRequest,pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));

    }



    // 특정 의약품 조회
    @GetMapping("/package-units/{medicine-package-unit-id}")
    public ResponseEntity<ApiResponse<MedicinePackageDetailResponse>> getDetailMedicine(@PathVariable(name = "medicine-package-unit-id") Long medicinePackageUnitId){

        return ResponseEntity.ok(ApiResponse.success(medicineSearchService.getDetail(medicinePackageUnitId)));

    }



    // http://localhost:10400/api/core/organizations/18/storages
    // GET http://localhost:10400/api/core/medicines/package-units/58581
    //GET http://localhost:10400/api/core/medicine-environment/package-units/58581


}
