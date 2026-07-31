package com.nhnacademy.inventory.medicines.medicine.controller;


import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/core")
public class MedicineController {

    private final MedicineApiService medicineApiService;

    @PostMapping("/admin/medicines/import")
    public ResponseEntity<ApiResponse<Void>> savedMedicines(){

        medicineApiService.savedAllMedicines();
        return ResponseEntity.ok(ApiResponse.ok());

    }





}
