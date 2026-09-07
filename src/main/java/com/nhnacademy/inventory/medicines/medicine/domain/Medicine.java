package com.nhnacademy.inventory.medicines.medicine.domain;

import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;
import com.nhnacademy.inventory.medicines.medicine.exception.CompanyNameRequiredException;
import com.nhnacademy.inventory.medicines.medicine.exception.ItemCodeRequiredException;
import com.nhnacademy.inventory.medicines.medicine.exception.ProductNameRequiredException;
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

    @Column(name = "product_name", length = 500, nullable = false)
    private String productName;

    @Column(name = "storage_method", length = 900)
    private String storageMethod;

    @Column(name = "validity_period", length = 100)
    private String validityPeriod;

    @Column(name = "narcotic_kind_code", length = 50)
    private String narcoticKindCode;

    @Column(name = "company_name", length = 200, nullable = false)
    private String companyName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Builder(access = AccessLevel.PRIVATE)
    private Medicine(String itemCode, String productName, String storageMethod, String validityPeriod,
                     String narcoticKindCode,
                     String companyName) {

        if(itemCode == null || itemCode.isBlank()){
            throw new ItemCodeRequiredException();
        }
        if(productName == null || productName.isBlank()){
            throw new ProductNameRequiredException();
        }
        if(companyName == null || companyName.isBlank()){
            throw new CompanyNameRequiredException();
        }


        this.itemCode = itemCode;
        this.productName = productName;
        this.storageMethod = storageMethod;
        this.validityPeriod = validityPeriod;
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

    public static Medicine create(
            String itemCode,
            String productName,
            String storageMethod,
            String validityPeriod,
            String narcoticKindCode,
            String companyName
    ) {
        return Medicine.builder()
                .itemCode(itemCode)
                .productName(productName)
                .storageMethod(storageMethod)
                .validityPeriod(validityPeriod)
                .narcoticKindCode(narcoticKindCode)
                .companyName(companyName)
                .build();
    }
    public boolean requiresNarcoticHandlingPermission() {
        return "마약".equals(narcoticKindCode)
                || "향정".equals(narcoticKindCode)
                || "향정신성의약품".equals(narcoticKindCode);
    }
}