package com.nhnacademy.inventory.inventories.transaction.domain;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "inventory_id", nullable = false)
    private MedicineInventory medicineInventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "before_quantity", nullable = false)
    private Integer beforeQuantity;

    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "memo", length = 500)
    private String memo;

    @Column(name = "processed_by", columnDefinition = "BINARY(16)", nullable = false)
    private byte[] processedBy;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Builder
    private StockTransaction(MedicineInventory medicineInventory, TransactionType transactionType,
                             Integer quantity, Integer beforeQuantity, Integer afterQuantity,
                             String reason, String memo, byte[] processedBy) {
        this.medicineInventory = medicineInventory;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.reason = reason;
        this.memo = memo;
        this.processedBy = processedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.processedAt = LocalDateTime.now();
    }
}
