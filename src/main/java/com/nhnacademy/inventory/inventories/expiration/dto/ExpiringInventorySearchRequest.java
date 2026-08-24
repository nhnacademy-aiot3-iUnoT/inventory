package com.nhnacademy.inventory.inventories.expiration.dto;

import com.nhnacademy.inventory.inventories.expiration.domain.ExpiringSearchFilterType;
import org.springframework.data.domain.Sort;

public record ExpiringInventorySearchRequest(
        Long storageId,
        ExpiringSearchFilterType filterType,
        Sort.Direction sortDirection
) {
    public ExpiringSearchFilterType getFilterType() {
        return filterType != null ? filterType : ExpiringSearchFilterType.ALL;
    }
}
