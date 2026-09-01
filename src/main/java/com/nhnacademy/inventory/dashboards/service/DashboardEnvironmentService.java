package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse.SensorEnvironmentResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse.ZoneEnvironmentResponse;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ZoneInfoResponse;
import com.nhnacademy.inventory.organizations.zone.service.ThresholdService;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import com.nhnacademy.inventory.reports.environment.client.RuleEngineApiClient;
import com.nhnacademy.inventory.reports.environment.dto.SensorDailyStatResponse;
import com.nhnacademy.inventory.reports.environment.dto.SensorLatestResponse;
import com.nhnacademy.inventory.reports.environment.dto.StorageDailySummaryResponse;
import com.nhnacademy.inventory.reports.environment.dto.ZoneDailySummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 저장소 환경 현황.
 * 구역·임계값(인벤토리 DB) + 현재 측정값·일별 통계(룰엔진 internal API)를 여기서 합쳐 내려보낸다.
 *
 * 임계값이 등록되지 않은 센서도 측정값이 들어오면 함께 담는다.
 * 임계값만 기준으로 만들면 조도처럼 기준이 없는 센서가 화면에서 사라진다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardEnvironmentService {

    private static final String DOOR = "DOOR";

    private final DashboardScopeResolver scopeResolver;
    private final StorageService storageService;
    private final ZoneService zoneService;
    private final ThresholdService thresholdService;
    private final RuleEngineApiClient ruleEngineApiClient;

    public DashboardEnvironmentResponse getEnvironment(Long storageId) {
        scopeResolver.verifyStorageAccess(storageId);

        Storage storage = storageService.validateMemberAndGetStorage(storageId);
        List<ZoneInfoResponse> zones = zoneService.getZones(storageId);

        Long organizationId = storage.getOrganization().getId();

        Map<Long, List<SensorLatestResponse>> latestByZone =
                ruleEngineApiClient.findLatestSensors(organizationId, storageId)
                        .stream()
                        .filter(sensor -> sensor.zoneId() != null)
                        .collect(Collectors.groupingBy(SensorLatestResponse::zoneId));

        // 룰엔진 일별 요약은 어제까지 집계된다. 조회에 실패하면 임계값과 현재값만 표시된다.
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Optional<StorageDailySummaryResponse> summary =
                ruleEngineApiClient.findDailySummaries(storageId, yesterday, yesterday)
                        .stream()
                        .max(Comparator.comparing(StorageDailySummaryResponse::date));

        Map<Long, ZoneDailySummaryResponse> summaryByZone = summary
                .map(StorageDailySummaryResponse::zones)
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.toMap(ZoneDailySummaryResponse::zoneId, Function.identity(), (a, b) -> a));

        List<ZoneEnvironmentResponse> zoneResponses = zones.stream()
                .map(zone -> toZoneResponse(
                        zone,
                        summaryByZone.get(zone.zoneId()),
                        latestByZone.getOrDefault(zone.zoneId(), List.of())))
                .toList();

        return new DashboardEnvironmentResponse(
                storage.getId(),
                storage.getName(),
                storage.getStatus(),
                summary.map(StorageDailySummaryResponse::date).orElse(null),
                zoneResponses
        );
    }

    private ZoneEnvironmentResponse toZoneResponse(
            ZoneInfoResponse zone,
            ZoneDailySummaryResponse summary,
            List<SensorLatestResponse> latestOfZone
    ) {
        Map<String, SensorLatestResponse> latestByType = latestOfZone.stream()
                .collect(Collectors.toMap(
                        sensor -> normalize(sensor.sensorType()), Function.identity(), (a, b) -> a));

        Map<String, SensorDailyStatResponse> statByType = summary == null
                ? Map.of()
                : summary.sensorStats().stream()
                        .collect(Collectors.toMap(
                                stat -> normalize(stat.sensorType()), Function.identity(), (a, b) -> a));

        // 센서 타입별로 한 줄씩. 임계값이 있는 것부터 넣고, 측정값만 있는 타입을 뒤에 채운다.
        Map<String, SensorEnvironmentResponse> sensors = new LinkedHashMap<>();

        for (ThresholdInfoResponse threshold : thresholdService.getThresholds(zone.zoneId())) {
            String type = normalize(threshold.sensorTypeName());

            if (DOOR.equals(type)) {
                continue;
            }

            sensors.put(type, toSensorResponse(
                    threshold.sensorTypeName(), threshold, statByType.get(type), latestByType.get(type)));
        }

        for (SensorLatestResponse latest : latestOfZone) {
            String type = normalize(latest.sensorType());

            if (DOOR.equals(type)) {
                continue;
            }

            sensors.putIfAbsent(type, toSensorResponse(
                    latest.sensorType(), null, statByType.get(type), latest));
        }

        return new ZoneEnvironmentResponse(
                zone.zoneId(),
                zone.name(),
                zone.status(),
                zone.envStatus(),
                List.copyOf(sensors.values()),
                doorOpened(latestByType.get(DOOR))
        );
    }

    private SensorEnvironmentResponse toSensorResponse(
            String sensorType,
            ThresholdInfoResponse threshold,
            SensorDailyStatResponse stat,
            SensorLatestResponse latest
    ) {
        BigDecimal min = threshold == null ? null : threshold.minValue();
        BigDecimal max = threshold == null ? null : threshold.maxValue();

        String unit = latest != null && latest.unit() != null && !latest.unit().isBlank()
                ? latest.unit()
                : (stat == null ? null : stat.unit());

        return new SensorEnvironmentResponse(
                sensorType,
                unit,
                min,
                max,
                latest == null ? null : latest.value(),
                stat == null ? null : stat.avg(),
                latest != null || stat != null
        );
    }

    /** 룰엔진은 문 상태를 0(닫힘) / 1(열림) 로 보낸다. */
    private Boolean doorOpened(SensorLatestResponse latest) {
        if (latest == null || latest.value() == null) {
            return null;
        }

        return latest.value() > 0.0;
    }

    /** 인벤토리는 "TEMPERATURE", 룰엔진은 "temperature" 를 쓰므로 대문자로 맞춘다. */
    private String normalize(String sensorType) {
        return sensorType == null ? "" : sensorType.trim().toUpperCase();
    }
}
