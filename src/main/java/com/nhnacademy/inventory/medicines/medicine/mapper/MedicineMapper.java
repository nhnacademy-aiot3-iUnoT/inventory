package com.nhnacademy.inventory.medicines.medicine.mapper;

import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MedicineMapper {

    public Medicine toMedicineEntity(MedicineResponse medicineResponse){

        return Medicine.createBuilder()
                .itemCode(parse(medicineResponse.itemCode()))
                .productName(parse(medicineResponse.productName()))
                .companyName(parse(medicineResponse.companyName()))
                .storageMethod(parse(medicineResponse.storageMethod()))
                .validityPeriod(parse(medicineResponse.validityPeriod()))
                .ingredientContent(parse(medicineResponse.ingredientContent()))
                .narcoticKindCode(parse(medicineResponse.narcoticKindCode()))
                .build();

    }


    public List<MedicinePackageUnit> toPackageUnitEntities(Medicine medicine, MedicineResponse medicineResponse){

        return medicineResponse.packageUnits().stream()
                .filter(unit -> unit != null && !unit.isBlank())
                .map(unit -> MedicinePackageUnit.create(medicine,unit))
                .toList();

    }


    private String parse(String value){

        if(value == null || value.isBlank()){
            return null;
        }

        return value.trim();

    }



}
