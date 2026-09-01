package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardExpiringResponse;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.expiration.service.ExpirationSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardExpiringService {

    private static final int DEFAULT_WITHIN_DAYS = 30;
    private static final int URGENT_DAYS = 7;
    private static final int DEFAULT_SIZE = 5;
    private static final int MAX_SIZE = 50;

    private final DashboardScopeResolver scopeResolver;
    private final ExpirationSearchService expirationSearchService;

    // 유통기한이 임박한 재고목록 조회(7일, 30일 미만)
    public DashboardExpiringResponse getExpiring(Long departmentId, Integer withinDays, Integer size) {
        List<Long> storageIds = scopeResolver.resolveStorageIds(departmentId); // 접근가능 한 저장소Id 조회

        if (storageIds.isEmpty()) { // 저장소 조회실패시 빈리스트 반환
            return DashboardExpiringResponse.empty();
        }

        int days = (withinDays == null || withinDays <= 0) ? DEFAULT_WITHIN_DAYS : withinDays; // 조회 기간 설정
        int pageSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE); // 페이지 크기 설정

        Page<ExpiringInventoryResponse> page = expirationSearchService.searchExpiringByStorages( //
                storageIds, days, PageRequest.of(0, pageSize));

        long within7 = expirationSearchService.countExpiringByStorages(storageIds, URGENT_DAYS); // 7일 이내 만료 개수 조회
        long within30 = days == DEFAULT_WITHIN_DAYS // 30일 이내 만료 개수 조회
                ? page.getTotalElements()
                : expirationSearchService.countExpiringByStorages(storageIds, DEFAULT_WITHIN_DAYS);

        return new DashboardExpiringResponse(page.getContent(), within7, within30);
    }
}
