package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSaveRequest;
import com.nhnacademy.inventory.organizations.zone.exception.ThresholdInvalidRangeException;
import com.nhnacademy.inventory.organizations.zone.exception.ThresholdNotFoundException;
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
import java.util.UUID;

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
    private Organization otherOrganization;
    private OrganizationMember approvedMember;
    private OrganizationMember otherMember;
    private Storage storage;
    private Zone zone;
    private ZoneThreshold threshold;
    private SensorType sensorType;

    @BeforeEach
    void setUp() throws Exception {
        organization = TestFixtures.createOrganization("테스트 조직1", "0123456789");
        setId(organization, 1L);

        otherOrganization = TestFixtures.createOrganization("테스트 조직2", "1234567890");
        setId(organization, 2L);

        approvedMember = TestFixtures.createOrganizationMember(organization);
        setId(approvedMember, 11L);

        otherMember = TestFixtures.createOrganizationMember(otherOrganization);
        setId(otherMember, 22L);

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
    void saveThreshold_Success1() {
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
    void saveThreshold_Success2() {
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
    @DisplayName("센서타입에 따른 임계값 저장 실패 - 권한없음(멤버아님)")
    void saveThreshold_Fail_NotMember() {
        ThresholdSaveRequest request = new ThresholdSaveRequest(
                sensorType.getSensorTypeId(),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                5
        );

        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
                        thresholdService.saveThreshold(zone.getId(), UUID.randomUUID(), request)
        );
    }

    @Test
    @DisplayName("센서타입에 따른 임계값 저장 실패 - 권한없음(조직 아이디 불일치)")
    void saveThreshold_Fail_OtherZone() {
        ThresholdSaveRequest request = new ThresholdSaveRequest(
                sensorType.getSensorTypeId(),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30),
                5
        );

        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));

        assertThrowsExactly(ForbiddenException.class, () ->
                thresholdService.saveThreshold(zone.getId(), otherMember.getAccountUuid(), request)
        );
    }

    @Test
    @DisplayName("센서타입에 따른 임계값 저장 실패 - 임계값 범위 5초 미만")
    void saveThreshold_Fail_InvalidRange() {
        ThresholdSaveRequest request = new ThresholdSaveRequest(
                sensorType.getSensorTypeId(),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(23),
                5
        );

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));
        given(sensorTypeRepository.findById(sensorType.getSensorTypeId()))
                .willReturn(Optional.of(sensorType));

        assertThrowsExactly(ThresholdInvalidRangeException.class, () ->
                thresholdService.saveThreshold(zone.getId(), approvedMember.getAccountUuid(), request)
        );

        verify(thresholdRepository, never()).save(any());
    }

    @Test
    @DisplayName("임계값 조회 성공 테스트")
    void getThresholds_Success() {
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
    @DisplayName("임계값 조회 성공 테스트(빈 리스트)")
    void getThresholds_Success_empty() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));
        given(thresholdRepository.findAllByZone(zone))
                .willReturn(List.of());

        List<ThresholdInfoResponse> responses = thresholdService.getThresholds(zone.getId(), approvedMember.getAccountUuid());

        assertEquals(0, responses.size());
    }

    @Test
    @DisplayName("임계값 조회 실패 - 권한없음(멤버아님)")
    void getThresholds_Fail_NotMember() {
        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
                thresholdService.getThresholds(zone.getId(), UUID.randomUUID())
        );
    }

    @Test
    @DisplayName("임계값 조회 실패 - 권한없음(조직 아이디 불일치)")
    void getThresholds_Fail_IdMismatch() {
        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));

        assertThrowsExactly(ForbiddenException.class, () ->
                thresholdService.getThresholds(zone.getId(), otherMember.getAccountUuid())
        );
    }

    @Test
    @DisplayName("임계값 삭제 성공 테스트")
    void deleteThreshold_Success() {
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

    @Test
    @DisplayName("임계값 삭제 실패 - 권한없음(멤버아님)")
    void deleteThreshold_Fail_NotMember() {
        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
                thresholdService.deleteThreshold(zone.getId(), threshold.getZoneThresholdId(), UUID.randomUUID())
        );
    }

    @Test
    @DisplayName("임계값 삭제 실패 - 권한없음(조직 아이디 불일치)")
    void deleteThreshold_Fail_IdMismatch() {
        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));

        assertThrowsExactly(ForbiddenException.class, () ->
                thresholdService.deleteThreshold(zone.getId(), threshold.getZoneThresholdId(), otherMember.getAccountUuid())
        );
    }

    @Test
    @DisplayName("임계값 삭제 실패 - 존재하지않는 임계값아이디")
    void deleteThreshold_Fail_NotFound_Threshold() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(zoneRepository.findById(zone.getId()))
                .willReturn(Optional.of(zone));
        given(thresholdRepository.findByZoneThresholdIdAndZone(anyLong(), any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ThresholdNotFoundException.class, () ->
                thresholdService.deleteThreshold(zone.getId(), 3333L, approvedMember.getAccountUuid())
        );

        verify(thresholdRepository, never()).delete(threshold);
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}