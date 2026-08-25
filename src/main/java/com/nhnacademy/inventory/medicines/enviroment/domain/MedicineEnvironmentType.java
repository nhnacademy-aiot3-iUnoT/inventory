package com.nhnacademy.inventory.medicines.enviroment.domain;

import com.nhnacademy.inventory.medicines.enviroment.exception.EnvironmentRangeInvalidException;
import com.nhnacademy.inventory.medicines.enviroment.exception.EnvironmentRangeRequiredException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "medicine_environment_types",

        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_medicine_environment_type",
                    columnNames = {
                            "medicine_environment_standard_id",
                            "environment_type"
                    }
            )
        }
)
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

    @JdbcTypeCode(SqlTypes.VARCHAR)
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


        if(min == null || max == null){
            throw new EnvironmentRangeRequiredException();
        }

        if(min.compareTo(max) >= 0){
            throw new EnvironmentRangeInvalidException();

        }

        this.medicineEnvironmentStandard = medicineEnvironmentStandard;
        this.environmentType = environmentType;
        this.min = min;
        this.max = max;
    }


    public static MedicineEnvironmentType create(MedicineEnvironmentStandard medicineEnvironmentStandard,
                                        EnvironmentType environmentType,
                                        BigDecimal min,
                                        BigDecimal max
                                        ){
        return new MedicineEnvironmentType(medicineEnvironmentStandard,environmentType,min,max);

    }




}
