package com.nhnacademy.inventory.inventories.expiration.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventorySearchRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpirationSearchService {

    private final MedicineInventoryRepository inventoryRepository;
    private final OrganizationAccessService organizationAccessService;

    public Page<ExpiringInventoryResponse> searchExpiringInventories(ExpiringInventorySearchRequest request, Pageable pageable){

        Sort sort = Sort.by(
                request.sortDirection() != null ? request.sortDirection() : Sort.Direction.ASC,
                "expirationDate"
        );

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Long organizationId = organizationAccessService.getCurrentMember().getOrganization().getId();

        return inventoryRepository.findExpiringInventories(organizationId, request, sortedPageable);
    }

    /**
     * 저장소 여러 곳을 묶어 임박 재고를 조회한다. 대시보드가 부서 단위로 볼 때 쓴다.
     */
    public Page<ExpiringInventoryResponse> searchExpiringByStorages(
            List<Long> storageIds, int withinDays, Pageable pageable) {
        return inventoryRepository.findExpiringInventoriesByStorageIds(
                currentOrganizationId(), storageIds, withinDays, pageable);
    }

    /**
     * 저장소 여러 곳의 임박 재고 수.
     */
    public long countExpiringByStorages(List<Long> storageIds, int withinDays) {
        return inventoryRepository.countExpiringWithinDays(
                currentOrganizationId(), storageIds, withinDays);
    }

    private Long currentOrganizationId() {
        return organizationAccessService.getCurrentMember().getOrganization().getId();
    }
}
