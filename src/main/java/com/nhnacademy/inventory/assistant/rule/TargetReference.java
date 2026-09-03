package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.domain.TargetType;

// 프론트 라우트가 저장소 하위로 중첩되어 있어 storageId 가 함께 필요하다.
public record TargetReference(
        TargetType type,
        Long storageId,
        Long targetId
) {
}
