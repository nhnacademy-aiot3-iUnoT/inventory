package com.nhnacademy.inventory.inventories.inventory.domain;

import com.nhnacademy.inventory.inventories.inventory.exception.InsufficientStockException;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "medicine_inventorys",
    uniqueConstraints = {@UniqueConstraint(
            name = "uk_medicine_inventory",
            columnNames = {
                    "medicine_package_unit_id",
                    "zone_id",
                    "lot_number",
                    "expiration_date"
            }

        )

    }

)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_package_unit_id", nullable = false)
    private MedicinePackageUnit medicinePackageUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "lot_number", length = 50, nullable = false)
    private String lotNumber;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Setter
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "management_status", length = 30, nullable = false)
    private ManagementStatus managementStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_review_at", nullable = false)
    private LocalDateTime lastReviewAt;

    @Builder
    private MedicineInventory(MedicinePackageUnit medicinePackageUnit, Zone zone, String lotNumber,
                              LocalDate expirationDate, Integer currentQuantity,
                              ManagementStatus managementStatus) {
        this.medicinePackageUnit = medicinePackageUnit;
        this.zone = zone;
        this.lotNumber = lotNumber;
        this.expirationDate = expirationDate;
        this.currentQuantity = currentQuantity;
        this.managementStatus = managementStatus;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastReviewAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public static MedicineInventory create(MedicinePackageUnit medicinePackageUnit, Zone zone, String lotNumber, LocalDate expirationDate, int quantity){

        return MedicineInventory.builder()
                .medicinePackageUnit(medicinePackageUnit)
                .zone(zone)
                .lotNumber(lotNumber.trim())
                .expirationDate(expirationDate)
                .currentQuantity(quantity)
                .managementStatus(ManagementStatus.NORMAL)
                .build();

    }

    public void increaseQuantity(int quantity){

        this.currentQuantity += quantity;
    }

    public void decreaseQuantity(int quantity) {
        if (this.currentQuantity < quantity) {
            throw new InsufficientStockException();
        }

        this.currentQuantity -= quantity;
        if (this.currentQuantity == 0) {
            this.managementStatus = ManagementStatus.DEPLETED;
        }
    }

    public void disposeQuantity(int quantity) {

        if (this.currentQuantity < quantity) {
            throw new InsufficientStockException();
        }

        this.currentQuantity -= quantity;

        if (this.currentQuantity == 0) {
            this.managementStatus = ManagementStatus.DISPOSAL;
        }
    }

    public void review(){
        this.lastReviewAt = LocalDateTime.now();
    }

}
