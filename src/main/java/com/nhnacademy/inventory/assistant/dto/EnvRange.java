package com.nhnacademy.inventory.assistant.dto;

import java.math.BigDecimal;

/**
 * 보관 환경의 허용 범위.
 * <p>
 * 의약품 기준(medicine_environment_types)과 구역 기준(zone_thresholds)이 컬럼 이름만 다르고
 * 의미는 같아서 하나의 형태로 맞춰 읽는다.
 * <p>
 * 구역 기준은 min, max 가 null 일 수 있다. 이는 "제한 없음"이 아니라 "설정하지 않음"이므로
 * 비교에서 제외한다.
 */
public record EnvRange(
        String environmentType,
        BigDecimal min,
        BigDecimal max
) {
}
