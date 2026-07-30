package com.nhnacademy.inventory.organizations.organization.domain;

public enum OrganizationStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    // 상태 변경 규칙
    public boolean canChangeTo(OrganizationStatus target) {
        return switch (this) {
            case PENDING, SUSPENDED -> false;
            case ACTIVE -> target == INACTIVE || target == SUSPENDED;
            case INACTIVE -> target == ACTIVE || target == SUSPENDED;
        };
    }
}
