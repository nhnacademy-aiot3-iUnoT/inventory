package com.nhnacademy.inventory.medicines.medicine.domain;

import com.nhnacademy.inventory.medicines.medicine.exception.MedicineRequiredException;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitRequiredException;
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

        if(medicine == null){
            throw new MedicineRequiredException("의약품은 필수 입니다.");
        }
        if(packUnit == null || packUnit.isBlank()){
            throw new PackUnitRequiredException("포장단위는 필수 입니다.");
        }


        this.medicine = medicine;
        this.packUnit = packUnit.trim();
    }

    public static MedicinePackageUnit create(Medicine medicine, String packUnit){

        return new MedicinePackageUnit(medicine, packUnit);
    }


}
