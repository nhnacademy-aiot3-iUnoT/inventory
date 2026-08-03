package com.nhnacademy.inventory.medicines.medicine.controller;


import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineDetailRequest;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineSearchRequest;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineApiService;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core")
public class MedicineController {

    private final MedicineApiService medicineApiService;
    private final MedicineSearchService medicineSearchService;


    @PostMapping("/admin/medicines/import")
    public ResponseEntity<ApiResponse<Void>> savedMedicines(){

        medicineApiService.savedAllMedicines();
        return ResponseEntity.ok(ApiResponse.ok());

    }

    // 의약품 정보 조회
    @GetMapping("/medicines")
    public ResponseEntity<ApiResponse<Page<MedicinePackageSearchResponse>>> getMedicines(@Valid @ModelAttribute MedicineSearchRequest medicineSearchRequest, Pageable pageable){

        return ResponseEntity.ok(ApiResponse.success(medicineSearchService.searchMedicines(medicineSearchRequest,pageable)));

    }

    // 특정 의약품 조회
    @GetMapping("/medicines/package-units/{medicinePackageUnitId}")
    public ResponseEntity<ApiResponse<MedicinePackageDetailResponse>> getDetailMedicine(@PathVariable Long medicinePackageUnitId){

        return ResponseEntity.ok(ApiResponse.success(medicineSearchService.getDetail(medicinePackageUnitId)));

    }


}
