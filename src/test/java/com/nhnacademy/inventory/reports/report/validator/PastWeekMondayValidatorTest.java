package com.nhnacademy.inventory.reports.report.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PastWeekMondayValidatorTest {

    private PastWeekMondayValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PastWeekMondayValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    @DisplayName("입력값이 null이면 @NotNull에 위임하므로 true를 반환한다.")
    void isValid_WhenValueIsNull_ReturnsTrue() {
        // when
        boolean result = validator.isValid(null, context);

        // then
        assertThat(result)
                .isTrue();
    }

    @Test
    @DisplayName("입력 날짜가 월요일이 아니면(예: 화요일) false를 반환한다.")
    void isValid_WhenValueIsNotMonday_ReturnsFalse() {
        // given - 지난주 화요일
        LocalDate lastTuesday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).plusDays(1);

        // when
        boolean result = validator.isValid(lastTuesday, context);

        // then
        assertThat(result)
                .isFalse();
    }

    @Test
    @DisplayName("입력 날짜가 이번 주 월요일이면 아직 마감되지 않았으므로 false를 반환한다.")
    void isValid_WhenValueIsCurrentWeekMonday_ReturnsFalse() {
        // given - 이번 주 월요일
        LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);

        // when
        boolean result = validator.isValid(currentMonday, context);

        // then
        assertThat(result)
                .isFalse();
    }

    @Test
    @DisplayName("입력 날짜가 미래 주차의 월요일이면 false를 반환한다.")
    void isValid_WhenValueIsFutureMonday_ReturnsFalse() {
        // given - 다음 주 월요일
        LocalDate nextMonday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1);

        // when
        boolean result = validator.isValid(nextMonday, context);

        // then
        assertThat(result)
                .isFalse();
    }

    @ParameterizedTest(name = "{0}주 전 월요일은 지난 주차이므로 유효하다.")
    @ValueSource(ints = {1, 2, 5, 10})
    @DisplayName("입력 날짜가 지난주 또는 그 이전의 월요일이면 true를 반환한다.")
    void isValid_WhenValueIsPastWeekMonday_ReturnsTrue(int weeksAgo) {
        // given
        LocalDate pastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(weeksAgo);

        // when
        boolean result = validator.isValid(pastMonday, context);

        // then
        assertThat(result)
                .isTrue();
    }
}
