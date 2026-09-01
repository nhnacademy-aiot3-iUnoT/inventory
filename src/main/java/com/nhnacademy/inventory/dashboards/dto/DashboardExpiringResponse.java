package com.nhnacademy.inventory.dashboards.dto;

import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;

import java.util.List;

/**
 * 대시보드 하단 "유통기한 임박 의약품" 카드.
 * 표에 뿌릴 목록과 헤더에 뿌릴 D-7 / D-30 카운트를 함께 준다.
 */
public record DashboardExpiringResponse(
        List<ExpiringInventoryResponse> items,
        long totalCount,
        long within7Count,
        long within30Count
) {
    public static DashboardExpiringResponse empty() {
        return new DashboardExpiringResponse(List.of(), 0L, 0L, 0L);
    }
}
