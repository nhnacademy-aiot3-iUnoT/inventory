package com.nhnacademy.inventory.inventories.inventory.dto;

import java.math.BigDecimal;

public record InventoryZoneEnvironmentResponse(

        Long storageId,
        String storageName,
        Long zoneId,
        String zoneName,
        BigDecimal minTemperature,
        BigDecimal maxTemperature,
        BigDecimal minHumidity,
        BigDecimal maxHumidity,
        BigDecimal minIlluminance,
        BigDecimal maxIlluminance


) {
}
