package com.nhnacademy.inventory.medicines.enviroment.domain;

import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentTypeRequest;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "medicine_environment_standards",
    uniqueConstraints = {

        @UniqueConstraint(

                name = "uk_medicine_environment_standard",
                columnNames = {
                        "organization_id",
                        "medicine_package_unit_id"
                }
        )
    }
)
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


    @OneToMany(
            mappedBy = "medicineEnvironmentStandard",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<MedicineEnvironmentType> environmentTypes = new ArrayList<>();


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

    public static MedicineEnvironmentStandard create(
            MedicinePackageUnit medicinePackageUnit,
            Organization organization,
            UUID createAccountId
    ){
        return new MedicineEnvironmentStandard(medicinePackageUnit,organization,createAccountId);

    }

    public void updateAccount(UUID updateAccountId){
        this.updateAccountId = updateAccountId;
    }


    public void addEnvironmentType(EnvironmentType environmentType, BigDecimal min, BigDecimal max){

        MedicineEnvironmentType type = MedicineEnvironmentType.create(this,environmentType,min,max);
        environmentTypes.add(type);


    }


    public void updateEnvironmentTypes(List<MedicineEnvironmentTypeRequest> requests){

        environmentTypes.clear();
        for(MedicineEnvironmentTypeRequest request : requests){
            addEnvironmentType(request.environmentType(),request.min(),request.max());

        }

    }





}