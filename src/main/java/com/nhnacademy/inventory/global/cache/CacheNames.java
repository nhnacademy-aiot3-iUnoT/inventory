package com.nhnacademy.inventory.global.cache;

public final class CacheNames {

    public static final String DEVICE_ZONE = "device-zone";

    public static final String ZONE_THRESHOLD = "zone-threshold";

    public static final String MEMBER_ORGANIZATION = "member-organization";

    public static final String ZONE_LOCATION = "zone-location";

    public static final String ZONE_ACTIVATION = "zone-activation";

    // 룰엔진이 Redis에 들고 있는 환경상태 판단 상태를 초기화하라는 신호.
    // 키는 "조직:저장소:구역" 형식이어야 한다.
    public static final String ZONE_DECISION_STATE = "zone-decision-state";

    public static String zoneDecisionStateKey(Long organizationId, Long storageId, Long zoneId) {
        return organizationId + ":" + storageId + ":" + zoneId;
    }


    private CacheNames() {
    }
}
