package com.nhnacademy.inventory.medicines.medicine.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "medicine.api")
public record MedicineProperties(

        String baseUrl,
        String detailPath,
        String serviceKey

) {
}
