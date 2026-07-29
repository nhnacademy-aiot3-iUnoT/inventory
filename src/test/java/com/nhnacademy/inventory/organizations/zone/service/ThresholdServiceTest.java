package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSaveRequest;
import com.nhnacademy.inventory.organizations.zone.repository.SensorTypeRepository;
import com.nhnacademy.inventory.organizations.zone.repository.ThresholdRepository;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ThresholdServiceTest {
    @Mock
    private ThresholdRepository thresholdRepository;
    @Mock
    private OrganizationMemberRepository memberRepository;
    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private SensorTypeRepository sensorTypeRepository;

    @InjectMocks
    private ThresholdService thresholdService;

    private Organization organization;
    private OrganizationMember approvedMember;
    private Storage storage;
    private Zone zone;
    private ZoneThreshold threshold;
    private SensorType sensorType;

    @BeforeEach
    void setUp() throws Exception {
        organization = TestFixtures.createOrganization("테스트 조직1", "0123456789");
        setId(organization, 1L);

        approvedMember = TestFixtures.createOrganizationMember(organization);
        setId(approvedMember, 11L);

        storage = TestFixtures.createStorage(organization, "테스트 저장소1");
        setId(storage, 111L);

        zone = TestFixtures.createZone(storage, "테스트 구역1");
        setId(zone, 1111L);

        sensorType = TestFixtures.createSensorType("테스트 센서");
        Field field = sensorType.getClass().getDeclaredField("sensorTypeId");
        field.setAccessible(true);
        field.set(sensorType, 1L);

        threshold = TestFixtures.createThreshold(zone, sensorType,
                BigDecimal.valueOf(0), BigDecimal.valueOf(30), 5);
        field = threshold.getClass().getDeclaredField("zoneThresholdId");
        field.setAccessible(true);
        field.set(threshold, 1L);
    }

    @Test
    @DisplayName("센서타입에 따른 임계값 저장 성공 테스트(없을 시 생성)")
    void saveThreshold1() {
        ThresholdSaveRequest request = new ThresholdSaveRequest(
                sensorType.getSensorTypeId(),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                5
        );

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));
        given(sensorTypeRepository.findById(sensorType.getSensorTypeId()))
                .willReturn(Optional.of(sensorType));
        given(thresholdRepository.findByZoneAndSensorType(zone, sensorType))
                .willReturn(Optional.empty());
        given(thresholdRepository.save(any(ZoneThreshold.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ThresholdInfoResponse response = thresholdService.saveThreshold(zone.getId(), approvedMember.getAccountUuid(), request);

        assertAll(
                () -> assertNotEquals(1L, response.zoneThresholdId()),
                () -> assertEquals(1111L, response.zoneId()),
                () -> assertEquals(1L, response.sensorTypeId()),
                () -> assertEquals(BigDecimal.valueOf(20), response.minValue()),
                () -> assertEquals(BigDecimal.valueOf(30), response.maxValue()),
                () -> assertEquals(5, response.alertDuration())
        );

        verify(thresholdRepository).save(any());
    }

    @Test
    @DisplayName("센서타입에 따른 임계값 저장 성공 테스트(있을 시 업데이트)")
    void saveThreshold2() {
        ThresholdSaveRequest request = new ThresholdSaveRequest(
                sensorType.getSensorTypeId(),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                5
        );

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));
        given(sensorTypeRepository.findById(sensorType.getSensorTypeId()))
                .willReturn(Optional.of(sensorType));
        given(thresholdRepository.findByZoneAndSensorType(zone, sensorType))
                .willReturn(Optional.of(threshold));

        ThresholdInfoResponse response = thresholdService.saveThreshold(zone.getId(), approvedMember.getAccountUuid(), request);

        assertAll(
                () -> assertEquals(1L, response.zoneThresholdId()),
                () -> assertEquals(1111L, response.zoneId()),
                () -> assertEquals(1L, response.sensorTypeId()),
                () -> assertEquals(BigDecimal.valueOf(20), response.minValue()),
                () -> assertEquals(BigDecimal.valueOf(30), response.maxValue()),
                () -> assertEquals(5, response.alertDuration())
        );

        verify(thresholdRepository, never()).save(any());
    }

    @Test
    @DisplayName("임계값 조회 성공 테스트")
    void getThresholds() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));
        given(thresholdRepository.findAllByZone(zone))
                .willReturn(List.of(threshold));

        List<ThresholdInfoResponse> responses = thresholdService.getThresholds(zone.getId(), approvedMember.getAccountUuid());

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(1L, responses.getFirst().sensorTypeId()),
                () -> assertEquals(BigDecimal.valueOf(0), responses.getFirst().minValue()),
                () -> assertEquals(BigDecimal.valueOf(30), responses.getFirst().maxValue()),
                () -> assertEquals(5, responses.getFirst().alertDuration())
        );
    }

    @Test
    @DisplayName("임계값 삭제 성공 테스트")
    void deleteThreshold() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));
        given(thresholdRepository.findByZoneThresholdIdAndZone(threshold.getZoneThresholdId(), zone))
                .willReturn(Optional.of(threshold));

        assertDoesNotThrow(() ->
                thresholdService.deleteThreshold(zone.getId(), threshold.getZoneThresholdId(), approvedMember.getAccountUuid())
        );

        verify(thresholdRepository).delete(threshold);
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}