package com.nhnacademy.inventory.medicines.enviroment.dto;

import jakarta.validation.constraints.AssertTrue;

import java.math.BigDecimal;

public record MedicineEnvTypeRequest(

        BigDecimal minTemperature,
        BigDecimal maxTemperature,
        BigDecimal minHumidity,
        BigDecimal maxHumidity,
        BigDecimal minIlluminance,
        BigDecimal maxIlluminance


) {

    @AssertTrue(message = "최소 한 개 이상의 환경기준을 입력해주세요.")
    public boolean isEnvironmentEntered() {
        return minTemperature != null
                || maxTemperature != null
                || minHumidity != null
                || maxHumidity != null
                || minIlluminance != null
                || maxIlluminance != null;
    }

    @AssertTrue(message = "온도 최소값과 최대값을 모두 입력해주세요.")
    public boolean isTempValid() {
        return isPairValid(minTemperature, maxTemperature);
    }

    @AssertTrue(message = "온도 최소값은 최대값보다 클 수 없습니다.")
    public boolean isTempRangeValid() {
        return isRangeValid(minTemperature, maxTemperature);
    }

    @AssertTrue(message = "습도 최소값과 최대값을 모두 입력해주세요.")
    public boolean isHumidityValid() {
        return isPairValid(minHumidity, maxHumidity);
    }

    @AssertTrue(message = "습도 최소값은 최대값보다 클 수 없습니다.")
    public boolean isHumidityRangeValid() {
        return isRangeValid(minHumidity, maxHumidity);
    }

    @AssertTrue(message = "조도 최소값과 최대값을 모두 입력해주세요.")
    public boolean isIlluminanceValid() {
        return isPairValid(minIlluminance, maxIlluminance);
    }

    @AssertTrue(message = "조도 최소값은 최대값보다 클 수 없습니다.")
    public boolean isIlluminanceRangeValid() {
        return isRangeValid(minIlluminance, maxIlluminance);
    }

    private static boolean isPairValid(BigDecimal min, BigDecimal max) {

        return (min == null && max == null)
                || (min != null && max != null);
    }

    private static boolean isRangeValid(BigDecimal min, BigDecimal max) {

        if (min == null || max == null) {
            return true;
        }

        return min.compareTo(max) <= 0;
    }
}






