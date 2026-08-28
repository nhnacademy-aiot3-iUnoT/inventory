package com.nhnacademy.inventory.enviroments.review.service;

import com.nhnacademy.inventory.enviroments.event.dto.EnvironmentEventItemResponse;
import com.nhnacademy.inventory.enviroments.event.repository.EnvironmentEventRepository;
import com.nhnacademy.inventory.enviroments.review.domain.EnvironmentReview;
import com.nhnacademy.inventory.enviroments.review.dto.InventoryReviewRequest;
import com.nhnacademy.inventory.enviroments.review.dto.ReviewHistoryDetailResponse;
import com.nhnacademy.inventory.enviroments.review.dto.ReviewHistorySummaryResponse;
import com.nhnacademy.inventory.enviroments.review.dto.UnderReviewInventoryResponse;
import com.nhnacademy.inventory.enviroments.review.exception.ReviewNotFoundException;
import com.nhnacademy.inventory.enviroments.review.repository.EnvironmentReviewRepository;
import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.InventoryNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnvironmentReviewService {
    private final EnvironmentReviewRepository environmentReviewRepository;
    private final EnvironmentEventRepository environmentEventRepository;
    private final MedicineInventoryRepository inventoryRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final StockTransactionService transactionService;
    private final StorageService storageService;

    public Page<UnderReviewInventoryResponse> getUnderReviewPage(Long storageId, Pageable pageable){
        List<Long> targetStorageIds = getTargetStorageIds(storageId);

        return environmentReviewRepository.getUnderReviewInventories(targetStorageIds, pageable);
    }

    @Transactional
    public void reviewInventory(Long inventoryId, InventoryReviewRequest request){
        MedicineInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(InventoryNotFoundException::new);

        storageService.checkStoragePermission(inventory.getZone().getStorage().getId());

        EnvironmentReview review = EnvironmentReview.builder()
                .medicineInventory(inventory)
                .reviewerId(UserContext.getUserUuid())
                .isOut(request.isOut())
                .quantityAtReview(inventory.getCurrentQuantity())
                .memo(request.memo())
                .build();

        environmentReviewRepository.save(review);

        inventory.review();

        if (request.isOut()){
            inventory.setManagementStatus(ManagementStatus.DISPOSAL);
            transactionService.createStockTransaction(new StockTransactionCommand(
                    inventory.getMedicinePackageUnit(),
                    inventory.getZone(),
                    TransactionType.DISPOSAL,
                    inventory.getCurrentQuantity(),
                    "검토중폐기",
                    request.memo(),
                    UserContext.getUserUuid()
            ));
        }else{
            inventory.setManagementStatus(ManagementStatus.NORMAL);
        }
    }

    public Page<ReviewHistorySummaryResponse> getReviewHistoryPage(Long storageId, Pageable pageable){
        List<Long> targetStorageIds = getTargetStorageIds(storageId);

        return environmentReviewRepository.getReviewHistories(targetStorageIds, pageable);
    }

    public ReviewHistoryDetailResponse getReviewDetail(Long environmentReviewId){
        EnvironmentReview review = environmentReviewRepository.findByIdWithFetch(environmentReviewId)
                .orElseThrow(ReviewNotFoundException::new);

        List<ReviewHistorySummaryResponse> inventoryReviewHistories = environmentReviewRepository
                .findAllByMedicineInventoryWithFetch(review.getMedicineInventory())
                .stream()
                .map(ReviewHistorySummaryResponse::from)
                .toList();

        List<EnvironmentEventItemResponse> environmentEvents = environmentEventRepository
                .findAllByZoneAndCreatedAtAfter(review.getMedicineInventory().getZone(), review.getMedicineInventory().getCreatedAt())
                .stream()
                .map(EnvironmentEventItemResponse::from)
                .toList();

        return ReviewHistoryDetailResponse.from(review, inventoryReviewHistories, environmentEvents);
    }

    private List<Long> getTargetStorageIds(Long storageId){
        OrganizationMember member = organizationMemberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        if(storageId != null){
            storageService.checkStoragePermission(storageId);
            return List.of(storageId);
        }else{
            return storageService.getAccessibleStorageIds(member);
        }
    }
}
