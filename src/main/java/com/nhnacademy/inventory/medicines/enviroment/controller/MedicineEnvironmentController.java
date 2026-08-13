package com.nhnacademy.inventory.medicines.enviroment.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentTypeResponse;
import com.nhnacademy.inventory.medicines.enviroment.service.EnvironmentTypeSearchService;
import com.nhnacademy.inventory.medicines.enviroment.service.MedicineEnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class MedicineEnvironmentController {

    private final EnvironmentTypeSearchService environmentTypeSearchService;
    private final MedicineEnvironmentService medicineEnvironmentService;

    // 환경기준 조회
    @GetMapping("/package-units/{package-unit-id}/medicine-environment-types")
    public ResponseEntity<ApiResponse<List<MedicineEnvironmentTypeResponse>>> getEnvironmentTypes(
            @PathVariable(name= "package-unit-id")Long medicinePackageUnitId){
       return ResponseEntity.ok(ApiResponse.success(environmentTypeSearchService.getTypes(medicinePackageUnitId)));
    }


    // 환경기준 수정
    @PutMapping("/package-units/{package-unit-id}/medicine-environment-standard")
    public ResponseEntity<Void> updateEnvironmentTypes(
            @PathVariable(name = "package-unit-id")Long packageUnitId,
            @Valid @RequestBody MedicineEnvironmentRequest request){
        medicineEnvironmentService.updateTypes(packageUnitId,request);
        return ResponseEntity.noContent().build();
    }


    // 환경기준 삭제
    @DeleteMapping("/medicine-environment-standards/{standard-id}")
    public ResponseEntity<Void> deleteEnvironmentTypes(
            @PathVariable(name = "standard-id")Long standardId){

        medicineEnvironmentService.deleteTypes(standardId);

        return ResponseEntity.noContent().build();
    }


}
