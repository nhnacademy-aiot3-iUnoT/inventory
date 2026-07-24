package com.nhnacademy.inventory.medicines.medicine.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long id;

    @Column(name = "item_code", length = 30, nullable = false, unique = true)
    private String itemCode;

    @Column(name = "product_name", length = 50, nullable = false)
    private String productName;

    @Column(name = "storage_method", length = 500, nullable = false)
    private String storageMethod;

    @Column(name = "validity_period", length = 100)
    private String validityPeriod;

    @Column(name = "ingredient_content", length = 50)
    private String ingredientContent;

    @Column(name = "storage_precautions", length = 100)
    private String storagePrecautions;

    @Column(name = "narcotic_kind_code", length = 50)
    private String narcoticKindCode;

    @Column(name = "company_name", length = 50, nullable = false)
    private String companyName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Medicine(String itemCode, String productName, String storageMethod, String validityPeriod,
                     String ingredientContent, String storagePrecautions, String narcoticKindCode,
                     String companyName) {
        this.itemCode = itemCode;
        this.productName = productName;
        this.storageMethod = storageMethod;
        this.validityPeriod = validityPeriod;
        this.ingredientContent = ingredientContent;
        this.storagePrecautions = storagePrecautions;
        this.narcoticKindCode = narcoticKindCode;
        this.companyName = companyName;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}