package com.nhnacademy.inventory.inventories.threshold.domain;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_thresholds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "low_stock_setting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_package_unit_id", nullable = false)
    private MedicinePackageUnit medicinePackageUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id", nullable = false)
    private Storage storage;

    @Column(name = "threshold", nullable = false)
    private Integer threshold;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    private StockThreshold(MedicinePackageUnit medicinePackageUnit, Storage storage,
                           Integer threshold, Boolean isActive){
        this.medicinePackageUnit = medicinePackageUnit;
        this.storage = storage;
        this.threshold = threshold;
        this.isActive = isActive;
    }
}
