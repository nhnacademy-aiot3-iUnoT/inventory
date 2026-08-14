package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdDetailResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSaveRequest;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSpecResponse;
import com.nhnacademy.inventory.organizations.zone.exception.ThresholdInvalidRangeException;
import com.nhnacademy.inventory.organizations.zone.exception.ThresholdNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.SensorTypeRepository;
import com.nhnacademy.inventory.organizations.zone.repository.ThresholdRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    private SensorTypeRepository sensorTypeRepository;
    @Mock
    private ZoneService zoneService;

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

    @Nested
    @DisplayName("임계값 저장 테스트")
    class saveThreshold{

        @Test
        @DisplayName("성공 테스트(없을 시 생성)")
        void success1() {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    sensorType.getSensorTypeId(),
                    BigDecimal.valueOf(20),
                    BigDecimal.valueOf(30),
                    5
            );

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(sensorTypeRepository.findById(sensorType.getSensorTypeId()))
                    .willReturn(Optional.of(sensorType));
            given(thresholdRepository.findByZoneAndSensorType(zone, sensorType))
                    .willReturn(Optional.empty());
            given(thresholdRepository.save(any(ZoneThreshold.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            ThresholdDetailResponse response = thresholdService.saveThreshold(zone.getId(), request);

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
        @DisplayName("성공 테스트(있을 시 업데이트)")
        void success2() {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    sensorType.getSensorTypeId(),
                    BigDecimal.valueOf(20),
                    BigDecimal.valueOf(30),
                    5
            );

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(sensorTypeRepository.findById(sensorType.getSensorTypeId()))
                    .willReturn(Optional.of(sensorType));
            given(thresholdRepository.findByZoneAndSensorType(zone, sensorType))
                    .willReturn(Optional.of(threshold));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            ThresholdDetailResponse response = thresholdService.saveThreshold(zone.getId(), request);

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
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    sensorType.getSensorTypeId(),
                    BigDecimal.valueOf(20),
                    BigDecimal.valueOf(30),
                    5
            );

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    thresholdService.saveThreshold(zone.getId(), request)
            );
        }


        @Test
        @DisplayName("실패 - 임계값 범위 5초 미만")
        void fail_InvalidRange() {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    sensorType.getSensorTypeId(),
                    BigDecimal.valueOf(20),
                    BigDecimal.valueOf(23),
                    5
            );
            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ThresholdInvalidRangeException.class, () ->
                    thresholdService.saveThreshold(zone.getId(), request)
            );

            verify(thresholdRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("임계값 조회 테스트")
    class getThresholds{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(zoneService.validateMemberAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(thresholdRepository.findAllByZone(zone))
                    .willReturn(List.of(threshold));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            List<ThresholdInfoResponse> responses = thresholdService.getThresholds(zone.getId());

            assertAll(
                    () -> assertEquals(1, responses.size()),
                    () -> assertEquals(1L, responses.getFirst().sensorTypeId()),
                    () -> assertEquals(BigDecimal.valueOf(0), responses.getFirst().minValue()),
                    () -> assertEquals(BigDecimal.valueOf(30), responses.getFirst().maxValue()),
                    () -> assertEquals(5, responses.getFirst().alertDuration())
            );
        }

        @Test
        @DisplayName("성공 테스트(빈 리스트)")
        void success_empty() {
            given(zoneService.validateMemberAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(thresholdRepository.findAllByZone(zone))
                    .willReturn(List.of());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            List<ThresholdInfoResponse> responses = thresholdService.getThresholds(zone.getId());

            assertEquals(0, responses.size());
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            given(zoneService.validateMemberAndGetZone(zone.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    thresholdService.getThresholds(zone.getId())
            );
        }
    }

    @Nested
    @DisplayName("임계값 조회(내부 api용 메서드) 테스트")
    class internalGetThresholds{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(thresholdRepository.findAllByZoneId(threshold.getZone().getId()))
                    .willReturn(List.of(threshold));

            List<ThresholdSpecResponse> responses = thresholdService.internalGetThresholds(threshold.getZone().getId());

            assertAll(
                    () -> assertEquals(1, responses.size()),
                    () -> assertEquals(1L, responses.getFirst().sensorTypeId()),
                    () -> assertEquals("테스트 센서", responses.getFirst().sensorTypeName()),
                    () -> assertEquals(BigDecimal.valueOf(0), responses.getFirst().minValue()),
                    () -> assertEquals(BigDecimal.valueOf(30), responses.getFirst().maxValue()),
                    () -> assertEquals(5, responses.getFirst().alertDuration())
            );
        }

        @Test
        @DisplayName("성공 테스트(빈 리스트)")
        void success_empty() {
            given(thresholdRepository.findAllByZoneId(zone.getId()))
                    .willReturn(List.of());

            List<ThresholdSpecResponse> responses = thresholdService.internalGetThresholds(threshold.getZone().getId());

            assertEquals(0, responses.size());
        }
    }

    @Nested
    @DisplayName("임계값 삭제 테스트")
    class deleteThreshold{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(thresholdRepository.findByZoneThresholdIdAndZone(threshold.getZoneThresholdId(), zone))
                    .willReturn(Optional.of(threshold));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertDoesNotThrow(() ->
                    thresholdService.deleteThreshold(zone.getId(), threshold.getZoneThresholdId())
            );

            verify(thresholdRepository).delete(threshold);
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    thresholdService.deleteThreshold(zone.getId(), threshold.getZoneThresholdId())
            );
        }

        @Test
        @DisplayName("실패 - 존재하지않는 임계값아이디")
        void fail_NotFoundThreshold() {
            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(thresholdRepository.findByZoneThresholdIdAndZone(anyLong(), any()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ThresholdNotFoundException.class, () ->
                    thresholdService.deleteThreshold(zone.getId(), 3333L)
            );

            verify(thresholdRepository, never()).delete(threshold);
        }
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}