package com.nhnacademy.inventory.enviroments.review.domain;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "environment_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnvironmentReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "environment_review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private MedicineInventory medicineInventory;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewer_id", columnDefinition = "BINARY(16)")
    private UUID reviewerId;

    @Column(name = "is_out", nullable = false)
    private Boolean isOut; // 폐기대기 / 정상 처리

    @Column(name = "quantity_at_review", nullable = false)
    private Integer quantityAtReview;

    @Column(name = "memo", length = 255)
    private String memo;

    @Builder
    private EnvironmentReview(MedicineInventory medicineInventory, UUID reviewerId,
                              Boolean isOut, Integer quantityAtReview, String memo) {
        this.medicineInventory = medicineInventory;
        this.reviewerId = reviewerId;
        this.isOut = isOut;
        this.quantityAtReview = quantityAtReview;
        this.memo = memo;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}