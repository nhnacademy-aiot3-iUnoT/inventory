package com.nhnacademy.inventory.medicines.enviroment.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;


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
    public boolean tempIsValid(){

        return isValid(minTemperature,maxTemperature);
    }

    @AssertTrue(message = "온도 최소값은 최대값보다 클 수 없습니다.")
    public boolean tempIsRangeValid(){

        return isRangeValid(minTemperature,maxTemperature);
    }

    @AssertTrue(message = "습도 최소값과 최대값 모두 입력해주세요.")
    public boolean humIsValid(){

        return isValid(minHumidity,maxHumidity);
    }

    @AssertTrue(message = "습도 최소값은 최대값보다 클 수 없습니다.")
    public boolean humIsRangeValid(){

        return isRangeValid(minHumidity,maxHumidity);
    }

    @AssertTrue(message = "조도 최소값과 최대값 모두 입력해주세요.")
    public boolean illIsValid(){

        return isValid(minIlluminance,maxIlluminance);
    }


    @AssertTrue(message = "조도 최소값은 최대값보다 클 수 없습니다.")
    public boolean illIsRangeValid(){

        return isRangeValid(minIlluminance,maxIlluminance);
    }



    private static boolean isValid(BigDecimal min, BigDecimal max){


        if(min == null && max == null){
            return true;
        }

        if(min != null && max != null){
            return true;
        }

        return false;

    }


    // min > max
    private static boolean isRangeValid(BigDecimal min, BigDecimal max){


        if(min == null || max == null){
            return true;
        }

        if(min.compareTo(max)<= 0){
            return true;
        }

        return false;
    }






}
