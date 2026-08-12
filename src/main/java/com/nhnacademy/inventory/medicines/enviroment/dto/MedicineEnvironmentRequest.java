package com.nhnacademy.inventory.medicines.enviroment.dto;

import jakarta.validation.constraints.AssertTrue;


import java.math.BigDecimal;

public record MedicineEnvironmentRequest(

        BigDecimal minTemperature,
        BigDecimal maxTemperature,
        BigDecimal minHumidity,
        BigDecimal maxHumidity,
        BigDecimal minIlluminance,
        BigDecimal maxIlluminance


) {

    @AssertTrue(message = "온도 최소값과 최대값 모두 입력해주세요.")
    public boolean isTempValid(){

        return isValid(minTemperature,maxTemperature);
    }

    @AssertTrue(message = "온도 최소값은 최대값보다 클 수 없습니다.")
    public boolean isTempRangeValid(){

        return isRangeValid(minTemperature,maxTemperature);
    }

    @AssertTrue(message = "습도 최소값과 최대값 모두 입력해주세요.")
    public boolean isHumValid(){

        return isValid(minHumidity,maxHumidity);
    }

    @AssertTrue(message = "습도 최소값은 최대값보다 클 수 없습니다.")
    public boolean isHumRangeValid(){

        return isRangeValid(minHumidity,maxHumidity);
    }

    @AssertTrue(message = "조도 최소값과 최대값 모두 입력해주세요.")
    public boolean isIllValid(){

        return isValid(minIlluminance,maxIlluminance);
    }


    @AssertTrue(message = "조도 최소값은 최대값보다 클 수 없습니다.")
    public boolean isIllRangeValid(){

        return isRangeValid(minIlluminance,maxIlluminance);
    }



    private static boolean isValid(BigDecimal min, BigDecimal max){


        if(min == null && max == null){
            return true;
        }

        return min != null && max != null;

    }


    // min > max
    private static boolean isRangeValid(BigDecimal min, BigDecimal max){


        if(min == null || max == null){
            return true;
        }

        return min.compareTo(max) <= 0;
    }






}
