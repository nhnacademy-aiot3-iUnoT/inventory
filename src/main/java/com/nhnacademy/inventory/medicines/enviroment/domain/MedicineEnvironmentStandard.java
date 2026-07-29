package com.nhnacademy.inventory.medicines.enviroment.domain;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medicine_environment_standards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicineEnvironmentStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_environment_standard_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_package_unit_id", nullable = false)
    private MedicinePackageUnit medicinePackageUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "create_account_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID createAccountId;

    @Column(name = "update_account_id", columnDefinition = "BINARY(16)")
    private UUID updateAccountId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private MedicineEnvironmentStandard(MedicinePackageUnit medicinePackageUnit, Organization organization,
                                        UUID createAccountId) {
        this.medicinePackageUnit = medicinePackageUnit;
        this.organization = organization;
        this.createAccountId = createAccountId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}