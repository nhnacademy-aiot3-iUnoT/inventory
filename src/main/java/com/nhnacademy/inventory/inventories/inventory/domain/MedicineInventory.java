package com.nhnacademy.inventory.inventories.inventory.domain;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "management_status", nullable = false)
    private ManagementStatus managementStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_")
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



}
