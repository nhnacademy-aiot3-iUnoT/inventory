package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardExpiringResponse;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 부서 기준 임박 의약품 목록. 기존 /expiring 은 조직·저장소 단위라 부서로 좁힐 수 없어 별도로 둔다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardExpiringService {

    private static final int DEFAULT_WITHIN_DAYS = 30;
    private static final int URGENT_DAYS = 7;
    private static final int MAX_SIZE = 50;

    private final DashboardScopeResolver scopeResolver;
    private final MedicineInventoryRepository inventoryRepository;

    public DashboardExpiringResponse getExpiring(Long departmentId, Integer withinDays, Integer size) {
        List<Long> storageIds = scopeResolver.resolveStorageIds(departmentId);

        if (storageIds.isEmpty()) {
            return DashboardExpiringResponse.empty();
        }

        Long organizationId = scopeResolver.getCurrentMember().getOrganization().getId();
        int days = (withinDays == null || withinDays <= 0) ? DEFAULT_WITHIN_DAYS : withinDays;
        int pageSize = (size == null || size <= 0) ? 5 : Math.min(size, MAX_SIZE);

        Page<ExpiringInventoryResponse> page = inventoryRepository.findExpiringInventoriesByStorageIds(
                organizationId, storageIds, days, PageRequest.of(0, pageSize));

        long within7 = inventoryRepository.countExpiringWithinDays(organizationId, storageIds, URGENT_DAYS);
        long within30 = days == DEFAULT_WITHIN_DAYS
                ? page.getTotalElements()
                : inventoryRepository.countExpiringWithinDays(organizationId, storageIds, DEFAULT_WITHIN_DAYS);

        return new DashboardExpiringResponse(
                page.getContent(),
                page.getTotalElements(),
                within7,
                within30
        );
    }
}
