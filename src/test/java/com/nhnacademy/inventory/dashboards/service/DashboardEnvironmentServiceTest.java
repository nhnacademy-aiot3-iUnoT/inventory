package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse.SensorEnvironmentResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardEnvironmentResponse.ZoneEnvironmentResponse;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ZoneInfoResponse;
import com.nhnacademy.inventory.organizations.zone.service.ThresholdService;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import com.nhnacademy.inventory.reports.environment.client.RuleEngineApiClient;
import com.nhnacademy.inventory.reports.environment.dto.SensorLatestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

/**
 * 임계값(인벤토리)과 실측값(룰엔진)을 서버에서 합치는 부분을 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class DashboardEnvironmentServiceTest {

    @Mock
    DashboardScopeResolver scopeResolver;

    @Mock
    StorageService storageService;

    @Mock
    ZoneService zoneService;

    @Mock
    ThresholdService thresholdService;

    @Mock
    RuleEngineApiClient ruleEngineApiClient;

    @InjectMocks
    DashboardEnvironmentService dashboardEnvironmentService;

    Storage storage;

    @BeforeEach
    void setUp() {
        Organization organization = Organization.create("1234567890", "NHN 메디컬센터");
        ReflectionTestUtils.setField(organization, "id", 7L);

        storage = Storage.builder()
                .organization(organization)
                .name("본원 냉장창고")
                .status(StorageStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(storage, "id", 100L);

        given(storageService.validateMemberAndGetStorage(100L)).willReturn(storage);
        given(zoneService.getZones(100L)).willReturn(List.of(
                new ZoneInfoResponse(11L, 100L, "냉장 보관실 A", ZoneStatus.ACTIVE, EnvStatus.NORMAL)));
        given(ruleEngineApiClient.findDailySummaries(anyLong(), any(), any())).willReturn(List.of());
    }

    @Test
    @DisplayName("임계값이 있는 센서에 현재 측정값이 함께 담긴다.")
    void mergesThresholdWithLatestValue() {
        given(thresholdService.getThresholds(11L)).willReturn(List.of(
                new ThresholdInfoResponse(1L, 11L, 1L, "TEMPERATURE",
                        new BigDecimal("2.0"), new BigDecimal("8.0"), 10)));
        given(ruleEngineApiClient.findLatestSensors(7L, 100L)).willReturn(List.of(
                new SensorLatestResponse(7L, "e1", 100L, 11L, "temperature", 10.4, "℃", "t")));

        SensorEnvironmentResponse sensor = sensorsOf(dashboardEnvironmentService.getEnvironment(100L)).get("TEMPERATURE");

        assertAll(
                () -> assertEquals(10.4, sensor.currentValue()),
                () -> assertEquals(new BigDecimal("2.0"), sensor.thresholdMin()),
                () -> assertEquals(new BigDecimal("8.0"), sensor.thresholdMax()),
                () -> assertEquals("℃", sensor.unit()),
                () -> assertTrue(sensor.sensorRegistered())
        );
    }

    @Test
    @DisplayName("임계값이 없어도 측정값이 들어오는 센서는 함께 내려간다.")
    void includesSensorWithoutThreshold() {
        given(thresholdService.getThresholds(11L)).willReturn(List.of());
        given(ruleEngineApiClient.findLatestSensors(7L, 100L)).willReturn(List.of(
                new SensorLatestResponse(7L, "e1", 100L, 11L, "illumination", 310.0, "lux", "t")));

        SensorEnvironmentResponse sensor = sensorsOf(dashboardEnvironmentService.getEnvironment(100L)).get("ILLUMINATION");

        assertAll(
                () -> assertEquals(310.0, sensor.currentValue()),
                () -> assertNull(sensor.thresholdMin()),
                () -> assertNull(sensor.thresholdMax()),
                () -> assertEquals("lux", sensor.unit())
        );
    }

    @Test
    @DisplayName("문은 센서 목록이 아니라 열림/닫힘 상태로 내려간다.")
    void doorIsReportedAsOpenState() {
        given(thresholdService.getThresholds(11L)).willReturn(List.of());
        given(ruleEngineApiClient.findLatestSensors(7L, 100L)).willReturn(List.of(
                new SensorLatestResponse(7L, "e1", 100L, 11L, "door", 1.0, "", "t")));

        ZoneEnvironmentResponse zone = dashboardEnvironmentService.getEnvironment(100L).zones().getFirst();

        assertAll(
                () -> assertTrue(zone.sensors().isEmpty()),
                () -> assertEquals(Boolean.TRUE, zone.doorOpened())
        );
    }

    @Test
    @DisplayName("문 센서가 없으면 문 상태는 null 이다.")
    void doorIsNullWhenNotMeasured() {
        given(thresholdService.getThresholds(11L)).willReturn(List.of());
        given(ruleEngineApiClient.findLatestSensors(7L, 100L)).willReturn(List.of());

        assertNull(dashboardEnvironmentService.getEnvironment(100L).zones().getFirst().doorOpened());
    }

    @Test
    @DisplayName("룰엔진 조회가 실패해도 임계값만으로 응답이 만들어진다.")
    void survivesRuleEngineFailure() {
        given(thresholdService.getThresholds(11L)).willReturn(List.of(
                new ThresholdInfoResponse(1L, 11L, 1L, "TEMPERATURE",
                        new BigDecimal("2.0"), new BigDecimal("8.0"), 10)));
        // 클라이언트가 실패를 흡수해 빈 목록을 준다
        given(ruleEngineApiClient.findLatestSensors(7L, 100L)).willReturn(List.of());

        SensorEnvironmentResponse sensor = sensorsOf(dashboardEnvironmentService.getEnvironment(100L)).get("TEMPERATURE");

        assertAll(
                () -> assertNull(sensor.currentValue()),
                () -> assertEquals(new BigDecimal("8.0"), sensor.thresholdMax()),
                () -> assertTrue(!sensor.sensorRegistered())
        );
    }

    private Map<String, SensorEnvironmentResponse> sensorsOf(DashboardEnvironmentResponse response) {
        return response.zones().getFirst().sensors().stream()
                .collect(Collectors.toMap(
                        sensor -> sensor.sensorType().toUpperCase(), Function.identity()));
    }
}
