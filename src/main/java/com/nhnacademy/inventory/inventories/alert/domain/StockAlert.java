package com.nhnacademy.inventory.inventories.alert.domain;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_alert_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_package_unit_id", nullable = false)
    private MedicinePackageUnit medicinePackageUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id", nullable = false)
    private Storage storage;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private StockAlert(MedicinePackageUnit medicinePackageUnit, Storage storage,
                       Integer stockQuantity, Boolean isRead){
        this.medicinePackageUnit = medicinePackageUnit;
        this.storage = storage;
        this.stockQuantity = stockQuantity;
        this.isRead = isRead;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

