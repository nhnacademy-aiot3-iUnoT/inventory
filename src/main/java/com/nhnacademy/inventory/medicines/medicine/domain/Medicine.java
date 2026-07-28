package com.nhnacademy.inventory.medicines.medicine.domain;

import com.nhnacademy.inventory.global.error.ErrorCode;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;
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

    @Column(name = "product_name", length = 50, nullable = false)
    private String productName;

    @Column(name = "storage_method", length = 500, nullable = false)
    private String storageMethod;

    @Column(name = "validity_period", length = 100)
    private String validityPeriod;

    @Column(name = "ingredient_content", length = 50)
    private String ingredientContent;


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
                     String ingredientContent, String narcoticKindCode,
                     String companyName) {

        if(itemCode == null || itemCode.isBlank()){
            throw new ItemCodeRequiredException(MedicineErrorCode.ITEM_CODE_REQUIRED);
        }
        if(productName == null || productName.isBlank()){
            throw new ProductNameRequiredException(MedicineErrorCode.PRODUCT_NAME_REQUIRED);
        }
        if(companyName == null || companyName.isBlank()){
            throw new CompanyNameRequiredException(MedicineErrorCode.COMPANY_NAME_REQUIRED);
        }


        this.itemCode = itemCode;
        this.productName = productName;
        this.storageMethod = storageMethod;
        this.validityPeriod = validityPeriod;
        this.ingredientContent = ingredientContent;
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

    @Builder(builderMethodName="createBuilder")
    public static Medicine create(String itemCode, String productName, String storageMethod, String validityPeriod,
                         String ingredientContent, String narcoticKindCode,
                         String companyName){


        return new Medicine(
                itemCode,
                productName,
                storageMethod,
                validityPeriod,
                ingredientContent,
                narcoticKindCode,
                companyName);
    }


}