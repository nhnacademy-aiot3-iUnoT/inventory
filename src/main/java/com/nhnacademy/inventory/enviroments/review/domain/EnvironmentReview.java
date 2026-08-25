package com.nhnacademy.inventory.enviroments.review.domain;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
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
    @JoinColumn(name = "medicine_package_unit_id", nullable = false)
    private MedicinePackageUnit medicinePackageUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

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
    private EnvironmentReview(MedicinePackageUnit medicinePackageUnit, Zone zone, UUID reviewerId,
                              Boolean isOut, Integer quantityAtReview, String memo) {
        this.medicinePackageUnit = medicinePackageUnit;
        this.zone = zone;
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