package com.nhnacademy.inventory.medicines.enviroment.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.service.EnvironmentTypeSearchService;
import com.nhnacademy.inventory.medicines.enviroment.service.MedicineEnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/medicine-environment/package-units")
@RequiredArgsConstructor
public class MedicineEnvironmentController {

    private final EnvironmentTypeSearchService environmentTypeSearchService;
    private final MedicineEnvironmentService medicineEnvironmentService;

    // 환경기준 조회
    @GetMapping("/{package-unit-id}")
    public ResponseEntity<ApiResponse<List<MedicineEnvironmentType>>> getEnvironmentTypes(
            @PathVariable(name= "package-unit-id")Long medicinePackageUnitId){

       return ResponseEntity.ok(ApiResponse.success(environmentTypeSearchService.getTypes(medicinePackageUnitId)));
    }


    // 환경기준 수정
    @PutMapping("/{package-unit-id}")
    public ResponseEntity<Void> updateEnvironmentTypes(
            @PathVariable(name = "package-unit-id")Long medicinePackageUnitId,
            @RequestBody MedicineEnvironmentRequest request){

        medicineEnvironmentService.updateTypes(medicinePackageUnitId,request);
        return ResponseEntity.noContent().build();
    }


    // 환경기준 삭제
    @DeleteMapping("/{package-unit-id}")
    public ResponseEntity<Void> deleteEnvironmentTypes(
            @PathVariable(name = "package-unit-id")Long medicinePackageUnitId){

        medicineEnvironmentService.deleteTypes(medicinePackageUnitId);

        return ResponseEntity.noContent().build();
    }


}
