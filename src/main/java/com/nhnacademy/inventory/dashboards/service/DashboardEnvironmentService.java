package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse.SensorEnvironmentResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse.ZoneEnvironmentResponse;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ZoneInfoResponse;
import com.nhnacademy.inventory.organizations.zone.service.ThresholdService;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import com.nhnacademy.inventory.reports.environment.client.RuleEngineApiClient;
import com.nhnacademy.inventory.reports.environment.dto.SensorLatestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 저장소 환경 현황 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardEnvironmentService {

    private static final String DOOR = "DOOR";

    private final DashboardScopeResolver scopeResolver;
    private final ZoneService zoneService;
    private final ThresholdService thresholdService;
    private final RuleEngineApiClient ruleEngineApiClient;

    public DashboardEnvironmentResponse getEnvironment(Long storageId) {
        // 저장소에 접근가능한지 검증및 해당 저장소를 조회한다
        Storage storage = scopeResolver.resolveStorage(storageId);

        // 해당 저장소의 구역들을 조회한다
        List<ZoneInfoResponse> zones = zoneService.getZones(storageId);

        // 해당 저장소의 최신 센서 데이터를 조회한다
        Map<Long, List<SensorLatestResponse>> latestByZone =
                ruleEngineApiClient.findLatestSensors(storage.getOrganization().getId(), storageId)
                        .stream()
                        .filter(sensor -> sensor.zoneId() != null)
                        .collect(Collectors.groupingBy(SensorLatestResponse::zoneId));

        return new DashboardEnvironmentResponse(
                storage.getId(),
                storage.getName(),
                storage.getStatus(),
                zones.stream()
                        .map(zone -> toZoneResponse(
                                zone, latestByZone.getOrDefault(zone.zoneId(), List.of())))
                        .toList()
        );
    }

    // 해당 구역의 센서 정보를 변환한다
    private ZoneEnvironmentResponse toZoneResponse(
            ZoneInfoResponse zone,
            List<SensorLatestResponse> latestOfZone
    ) {
        Map<String, SensorLatestResponse> latestByType =
                indexLatestSensorsByType(latestOfZone);

        Map<String, SensorEnvironmentResponse> sensors =
                createSensors(zone.zoneId(), latestOfZone, latestByType);

        return new ZoneEnvironmentResponse(
                zone.zoneId(),
                zone.name(),
                zone.status(),
                zone.envStatus(),
                List.copyOf(sensors.values()),
                doorOpened(latestByType.get(DOOR))
        );
    }

    private Map<String, SensorLatestResponse> indexLatestSensorsByType(
            List<SensorLatestResponse> latestSensors
    ) {
        return latestSensors.stream()
                .collect(Collectors.toMap(
                        sensor -> normalize(sensor.sensorType()),
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    // 해당 구역의 센서데이터 환경정보 생성
    private Map<String, SensorEnvironmentResponse> createSensors(
            Long zoneId,
            List<SensorLatestResponse> latestOfZone,
            Map<String, SensorLatestResponse> latestByType
    ) {
        Map<String, SensorEnvironmentResponse> sensors = new LinkedHashMap<>();

        addThresholdSensors(zoneId, latestByType, sensors);
        addLatestOnlySensors(latestOfZone, sensors);

        return sensors;
    }

    // 임계값 있는 센서 환경정보 생성
    private void addThresholdSensors(
            Long zoneId,
            Map<String, SensorLatestResponse> latestByType,
            Map<String, SensorEnvironmentResponse> sensors
    ) {
        for (ThresholdInfoResponse threshold :
                thresholdService.getThresholds(zoneId)) {

            String type = normalize(threshold.sensorTypeName());

            if (DOOR.equals(type)) {
                continue;
            }

            sensors.put(
                    type,
                    toSensorResponse(
                            threshold.sensorTypeName(),
                            threshold,
                            latestByType.get(type)
                    )
            );
        }
    }


    // 임계값 미포함된 센서 환경정보 생성
    private void addLatestOnlySensors(
            List<SensorLatestResponse> latestOfZone,
            Map<String, SensorEnvironmentResponse> sensors
    ) {
        for (SensorLatestResponse latest : latestOfZone) {
            String type = normalize(latest.sensorType());

            if (DOOR.equals(type)) {
                continue;
            }

            sensors.putIfAbsent(
                    type,
                    toSensorResponse(
                            latest.sensorType(),
                            null,
                            latest
                    )
            );
        }
    }

    private SensorEnvironmentResponse toSensorResponse(
            String sensorType, ThresholdInfoResponse threshold, SensorLatestResponse latest) {

        BigDecimal min = threshold == null ? null : threshold.minValue();
        BigDecimal max = threshold == null ? null : threshold.maxValue();

        return new SensorEnvironmentResponse(
                sensorType,
                latest == null ? null : latest.unit(),
                min,
                max,
                latest == null ? null : latest.value()
        );
    }

    // 문 센서 측정값으로 열림여부 판단
    private Boolean doorOpened(SensorLatestResponse latest) {
        if (latest == null || latest.value() == null) {
            return null;
        }

        return latest.value() > 0.0;
    }

    // 센서타입 표기형식 통일
    private String normalize(String sensorType) {
        return sensorType == null ? "" : sensorType.trim().toUpperCase();
    }
}
