package com.nhnacademy.inventory.chatbot.dto;

import com.querydsl.core.annotations.QueryProjection;

public record
ConsumptionSummaryRow(
        Long packageUnitId,
        Long storageId,
        Long totalQuantity
) {

    @QueryProjection
    public  ConsumptionSummaryRow{

    }
}
