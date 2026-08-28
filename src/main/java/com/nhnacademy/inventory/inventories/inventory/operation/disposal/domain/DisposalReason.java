package com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain;

public enum DisposalReason {
    EXPIRED("유통기한 만료"),
    DETERIORATED("변질"),
    OTHER("기타");

    private final String description;

    DisposalReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

