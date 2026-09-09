package com.nhnacademy.inventory.medicines.medicine.dto;

import java.util.List;

public record MedicineResponse(

        String itemCode,
        String productName,
        String companyName,
        String storageMethod,
        String validityPeriod,
        List<String> packageUnits,
        String narcoticKindCode


) {
}
