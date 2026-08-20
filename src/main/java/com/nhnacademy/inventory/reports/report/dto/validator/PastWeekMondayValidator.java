package com.nhnacademy.inventory.reports.report.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

public class PastWeekMondayValidator implements ConstraintValidator<PastWeekMonday, LocalDate> {

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true; // null 검증은 @NotNull에 위임
        }

        // 입력 날짜가 월요일인지 확인
        if (!Objects.equals(value.getDayOfWeek(), DayOfWeek.MONDAY)) {
            return false;
        }

        // 이번 주 월요일인지 계산
        LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);

        // 입력값이 이번주 월요일보다 과거인지 확인
        return value.isBefore(currentMonday);
    }
}
