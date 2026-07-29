package com.nhnacademy.inventory.inventories.transaction.domain;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
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
@Table(name = "stock_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_package_unit_id", nullable = false)
    private MedicinePackageUnit medicinePackageUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "memo", length = 500)
    private String memo;

    @Column(name = "processed_by", columnDefinition = "BINARY(16)", nullable = false)
    private UUID processedBy;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Builder
    private StockTransaction(MedicinePackageUnit medicinePackageUnit, Zone zone,
                             TransactionType transactionType, Integer quantity,
                             String reason, String memo, UUID processedBy) {
        this.medicinePackageUnit = medicinePackageUnit;
        this.zone = zone;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.reason = reason;
        this.memo = memo;
        this.processedBy = processedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.processedAt = LocalDateTime.now();
    }
}
