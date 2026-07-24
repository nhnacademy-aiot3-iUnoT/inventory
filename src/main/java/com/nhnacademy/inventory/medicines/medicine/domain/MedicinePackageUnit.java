package com.nhnacademy.inventory.medicines.medicine.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medicine_package_units")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicinePackageUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_package_unit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "pack_unit", length = 50, nullable = false)
    private String packUnit;

    @Builder
    private MedicinePackageUnit(Medicine medicine, String packUnit) {
        this.medicine = medicine;
        this.packUnit = packUnit;
    }
}
