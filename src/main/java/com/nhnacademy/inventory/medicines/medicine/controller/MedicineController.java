package com.nhnacademy.inventory.medicines.medicine.controller;


import com.nhnacademy.inventory.medicines.medicine.service.MedicineApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/core")
public class MedicineController {

    private final MedicineApiService medicineApiService;

    @PostMapping("/admin/medicines/import")
    public ResponseEntity<String> importMedicines(){

        medicineApiService.savedAllMedicines();
        return ResponseEntity.ok("의약품 저장 완료");

    }



}
