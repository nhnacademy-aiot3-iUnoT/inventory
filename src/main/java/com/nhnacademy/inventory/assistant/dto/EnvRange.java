package com.nhnacademy.inventory.assistant.dto;

import java.math.BigDecimal;

// 의약품 기준과 구역 기준을 같은 형태로 읽기 위한 타입. min, max 가 null 이면 미설정임
public record EnvRange(
        String environmentType,
        BigDecimal min,
        BigDecimal max
) {
}
