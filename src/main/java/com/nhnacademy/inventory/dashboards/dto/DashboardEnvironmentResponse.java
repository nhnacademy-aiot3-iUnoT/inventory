package com.nhnacademy.inventory.dashboards.dto;

import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * 선택한 저장소의 구역 센서 현황.
 * 구역·임계값은 인벤토리 DB에서, 현재 측정값은 룰엔진 internal API에서 가져와
 * 여기서 합쳐 내려보낸다. 프론트는 받은 그대로 그리기만 하면 된다.
 */
public record DashboardEnvironmentResponse(
        Long storageId,
        String storageName,
        StorageStatus storageStatus,
        List<ZoneEnvironmentResponse> zones
) {

    public record ZoneEnvironmentResponse(
            Long zoneId,
            String name,
            ZoneStatus zoneStatus,
            EnvStatus envStatus,
            List<SensorEnvironmentResponse> sensors,

            // 문 센서가 없거나 측정값이 없으면 null
            Boolean doorOpened
    ) {
    }

    /** 센서 타입 하나에 대한 임계값 + 현재값 */
    public record SensorEnvironmentResponse(
            String sensorType,
            String unit,

            // 임계값이 등록되지 않았으면 null
            BigDecimal thresholdMin,
            BigDecimal thresholdMax,

            // 룰엔진의 현재 측정값. 측정 기록이 없으면 null
            Double currentValue
    ) {
    }
}
