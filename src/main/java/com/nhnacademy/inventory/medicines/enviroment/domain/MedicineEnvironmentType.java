package com.nhnacademy.inventory.medicines.enviroment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "medicine_environment_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineEnvironmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_environment_type_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_environment_standard_id", nullable = false)
    private MedicineEnvironmentStandard medicineEnvironmentStandard;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment_type", length = 30, nullable = false)
    private EnvironmentType environmentType;

    @Column(name = "min", precision = 10, scale = 2, nullable = false)
    private BigDecimal min;

    @Column(name = "max", precision = 10, scale = 2, nullable = false)
    private BigDecimal max;

    @Builder
    private MedicineEnvironmentType(MedicineEnvironmentStandard medicineEnvironmentStandard,
                                    EnvironmentType environmentType, BigDecimal min, BigDecimal max) {
        this.medicineEnvironmentStandard = medicineEnvironmentStandard;
        this.environmentType = environmentType;
        this.min = min;
        this.max = max;
    }
}
