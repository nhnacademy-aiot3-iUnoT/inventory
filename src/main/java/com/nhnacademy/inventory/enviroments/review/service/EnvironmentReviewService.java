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
import com.nhnacademy.inventory.global.client.AccountClient;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.alert.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.InventoryNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalReason;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final AccountClient accountClient;
    private final ApplicationEventPublisher eventPublisher;

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
                    DisposalReason.DETERIORATED.toString(),
                    request.memo(),
                    UserContext.getUserUuid()
            ));

            eventPublisher.publishEvent(new StockOutboundCompletedEvent(
                    inventory.getZone().getId(),
                    inventory.getMedicinePackageUnit().getId()
            ));
        }else{
            inventory.setManagementStatus(ManagementStatus.NORMAL);
        }
    }

    public Page<ReviewHistorySummaryResponse> getReviewHistoryPage(Long storageId, Pageable pageable){
        List<Long> targetStorageIds = getTargetStorageIds(storageId);

        Page<ReviewHistorySummaryResponse> reviews = environmentReviewRepository.getReviewHistories(targetStorageIds, pageable);

        Map<UUID, String> names = loadProcessorNames(reviews.getContent());

        return reviews.map(review ->
                ReviewHistorySummaryResponse.of(review, names.get(review.reviewerId())));
    }

    public ReviewHistoryDetailResponse getReviewDetail(Long environmentReviewId){
        EnvironmentReview review = environmentReviewRepository.findByIdWithFetch(environmentReviewId)
                .orElseThrow(ReviewNotFoundException::new);

        List<EnvironmentReview> reviewList = environmentReviewRepository
                .findAllByMedicineInventoryWithFetch(review.getMedicineInventory());

        LocalDateTime startDateTime = reviewList.stream()
                .map(EnvironmentReview::getCreatedAt)
                .filter(createAt -> createAt.isBefore(review.getCreatedAt()))
                .max(LocalDateTime::compareTo)
                .orElseGet(() -> review.getMedicineInventory().getCreatedAt());

        LocalDateTime endDateTime = review.getCreatedAt();

        List<ReviewHistorySummaryResponse> inventoryReviewHistories = reviewList
                .stream()
                .sorted(Comparator.comparing(EnvironmentReview::getCreatedAt).reversed())
                .limit(5)
                .map(ReviewHistorySummaryResponse::from)
                .toList();

        List<EnvironmentEventItemResponse> environmentEvents = environmentEventRepository
                .findAllByZoneAndCreatedAtBetween(review.getMedicineInventory().getZone(), startDateTime, endDateTime)
                .stream()
                .map(EnvironmentEventItemResponse::from)
                .toList();

        List<AccountResponse> accounts = accountClient.findByUuids(List.of(review.getReviewerId()));

        return ReviewHistoryDetailResponse.from(review, inventoryReviewHistories, environmentEvents, accounts.getFirst().name());
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

    private Map<UUID, String> loadProcessorNames(List<ReviewHistorySummaryResponse> reviews) {
        List<UUID> uuids = reviews.stream()
                .map(ReviewHistorySummaryResponse::reviewerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (uuids.isEmpty()) {
            return Map.of();
        }

        try {
            List<AccountResponse> accounts = accountClient.findByUuids(uuids);

            if (accounts == null) {
                return Map.of();
            }

            return accounts.stream()
                    .filter(account -> account.accountUuid() != null && account.name() != null)
                    .collect(Collectors.toMap(
                            AccountResponse::accountUuid, AccountResponse::name, (a, b) -> a));
        } catch (Exception e) {
            log.warn("처리자 정보를 불러오지 못해 재고 변동 내역만 반환합니다. 처리자 수={}", uuids.size(), e);
            return Map.of();
        }
    }
}
