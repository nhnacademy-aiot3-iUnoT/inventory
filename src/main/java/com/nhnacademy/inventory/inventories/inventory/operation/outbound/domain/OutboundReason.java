package com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain;

public enum OutboundReason {

    DISPENSING("조제·처방 출고"),
    STORAGE_TRANSFER("저장소 이동"),
    RETURN_TO_SUPPLIER("공급처 반품"),
    OTHER("기타");

    private final String description;

    OutboundReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
