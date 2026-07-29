package com.nhnacademy.inventory.medicines.medicine.dto;

public record MedicineSaveResult(

        int requestedCount,
        int savedCount,
        int skippedCount


) {
}
