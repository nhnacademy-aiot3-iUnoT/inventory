package com.nhnacademy.inventory.organizations.sensor.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.sensor.domain.ZoneSensor;
import com.nhnacademy.inventory.organizations.sensor.dto.*;
import com.nhnacademy.inventory.organizations.sensor.exception.ZoneSensorAlreadyExistsException;
import com.nhnacademy.inventory.organizations.sensor.exception.ZoneSensorNotFoundException;
import com.nhnacademy.inventory.organizations.sensor.repository.ZoneSensorRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ZoneSensorServiceTest {

    @Mock
    private ZoneSensorRepository zoneSensorRepository;

    @Mock
    private ZoneService zoneService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ZoneSensorService zoneSensorService;

    private Organization organization;
    private Storage storage;
    private Zone zone;
    private ZoneSensor zoneSensor;

    @BeforeEach
    void setUp() throws Exception {
        organization = TestFixtures.createOrganization("테스트 조직1", "0123456789");
        setId(organization, 1L);

        storage = TestFixtures.createStorage(organization, "테스트 저장소1");
        setId(storage, 11L);

        zone = TestFixtures.createZone(storage, "테스트 구역1");
        setId(zone, 111L);

        zoneSensor = TestFixtures.createZoneSensor(zone, "테스트 디바이스", "테스트 센서");
        setId(zoneSensor, 1111L);
    }

    @Nested
    @DisplayName("구역 센서 생성 테스트")
    class createZoneSensor{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            ZoneSensorCreateRequest request = new ZoneSensorCreateRequest(
                    "생성 테스트 디바이스", "생성 센서 이름", "테스트 설명");

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(zoneSensorRepository.existsByDeviceEui(request.deviceEui()))
                    .willReturn(false);
            given(zoneSensorRepository.save(any(ZoneSensor.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            ZoneSensorDetailResponse response = zoneSensorService.createZoneSensor(
                    zone.getId(),
                    request
            );

            assertAll(
                    () -> assertEquals(111L, response.zoneId()),
                    () -> assertEquals("생성 테스트 디바이스", response.deviceEui()),
                    () -> assertEquals("생성 센서 이름", response.name()),
                    () -> assertEquals("테스트 설명", response.description())
            );
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            ZoneSensorCreateRequest request = new ZoneSensorCreateRequest(
                    "생성 테스트 디바이스", "생성 센서 이름", "테스트 설명");

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willThrow(new ForbiddenException());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneSensorService.createZoneSensor(
                            zone.getId(),
                            request
                    )
            );

            verify(zoneSensorRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 이미 등록된 디바이스")
        void fail_AlreadyExists() {
            ZoneSensorCreateRequest request = new ZoneSensorCreateRequest(
                    "생성 테스트 디바이스", "생성 센서 이름", "테스트 설명");

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(zoneSensorRepository.existsByDeviceEui(request.deviceEui()))
                    .willReturn(true);

            assertThrowsExactly(ZoneSensorAlreadyExistsException.class, () ->
                    zoneSensorService.createZoneSensor(
                            zone.getId(),
                            request
                    )
            );

            verify(zoneSensorRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("구역 센서 조회 테스트")
    class getZoneSensors{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(zoneService.validateMemberAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(zoneSensorRepository.findAllByZone(zone))
                    .willReturn(List.of(zoneSensor));

            List<ZoneSensorInfoResponse> responses = zoneSensorService.getZoneSensors(zone.getId());

            assertAll(
                    () -> assertEquals(1, responses.size()),
                    () -> assertEquals(111L, responses.getFirst().zoneId()),
                    () -> assertEquals("테스트 디바이스", responses.getFirst().deviceEui()),
                    () -> assertEquals("테스트 센서", responses.getFirst().name())
            );
        }

        @Test
        @DisplayName("성공 테스트 (빈 리스트)")
        void success_empty() {
            given(zoneService.validateMemberAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(zoneSensorRepository.findAllByZone(zone))
                    .willReturn(List.of());

            List<ZoneSensorInfoResponse> responses = zoneSensorService.getZoneSensors(zone.getId());

            assertEquals(0, responses.size());
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            given(zoneService.validateMemberAndGetZone(zone.getId()))
                    .willThrow(new ForbiddenException());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneSensorService.getZoneSensors(zone.getId())
            );
        }
    }

    @Nested
    @DisplayName("센서 위치 정보 조회 테스트")
    class getDeviceLocation{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(zoneSensorRepository.findByDeviceEuiWithLocation(zoneSensor.getDeviceEui()))
                    .willReturn(Optional.of(zoneSensor));

            DeviceLocationResponse response = zoneSensorService.getDeviceLocation(zoneSensor.getDeviceEui());

            assertAll(
                    () -> assertEquals(1L, response.organizationId()),
                    () -> assertEquals(11L, response.storageId()),
                    () -> assertEquals(111L, response.zoneId()),
                    () -> assertEquals("테스트 디바이스", response.deviceEui())
            );
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 센서")
        void fail_NotFound() {
            given(zoneSensorRepository.findByDeviceEuiWithLocation(zoneSensor.getDeviceEui()))
                    .willReturn(Optional.empty());

            assertThrowsExactly(ZoneSensorNotFoundException.class, () ->
                    zoneSensorService.getDeviceLocation(zoneSensor.getDeviceEui())
            );
        }
    }

    @Nested
    @DisplayName("구역 센서 정보 업데이트 테스트")
    class updateZoneSensorInfo{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            ZoneSensorUpdateRequest request = new ZoneSensorUpdateRequest("수정된 센서 이름", "수정된 설명");

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(zoneSensorRepository.findByIdAndZone(zoneSensor.getId(), zone))
                    .willReturn(Optional.of(zoneSensor));

            ZoneSensorDetailResponse response = zoneSensorService.updateZoneSensorInfo(
                    zone.getId(),
                    zoneSensor.getId(),
                    request
            );

            assertAll(
                    () -> assertEquals(111L, response.zoneId()),
                    () -> assertEquals("테스트 디바이스", response.deviceEui()),
                    () -> assertEquals("수정된 센서 이름", response.name()),
                    () -> assertEquals("수정된 설명", response.description())
            );
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            ZoneSensorUpdateRequest request = new ZoneSensorUpdateRequest("수정된 센서 이름", "수정된 설명");

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willThrow(new ForbiddenException());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneSensorService.updateZoneSensorInfo(zone.getId(), zoneSensor.getId(), request)
            );
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 센서")
        void fail_NotFound() {
            ZoneSensorUpdateRequest request = new ZoneSensorUpdateRequest("수정된 센서 이름", "수정된 설명");

            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(zoneSensorRepository.findByIdAndZone(zoneSensor.getId(), zone))
                    .willReturn(Optional.empty());

            assertThrowsExactly(ZoneSensorNotFoundException.class, () ->
                    zoneSensorService.updateZoneSensorInfo(zone.getId(), zoneSensor.getId(), request)
            );
        }
    }

    @Nested
    @DisplayName("구역 센서 삭제 테스트")
    class deleteZoneSensor{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(zoneSensorRepository.findByIdAndZone(zoneSensor.getId(), zone))
                    .willReturn(Optional.of(zoneSensor));

            zoneSensorService.deleteZoneSensor(zone.getId(), zoneSensor.getId());

            verify(zoneSensorRepository).delete(zoneSensor);
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willThrow(new ForbiddenException());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneSensorService.deleteZoneSensor(zone.getId(), zoneSensor.getId())
            );
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 센서")
        void fail_NotFound() {
            given(zoneService.validateOwnerAndGetZone(zone.getId()))
                    .willReturn(zone);
            given(zoneSensorRepository.findByIdAndZone(zoneSensor.getId(), zone))
                    .willReturn(Optional.empty());

            assertThrowsExactly(ZoneSensorNotFoundException.class, () ->
                    zoneSensorService.deleteZoneSensor(zone.getId(), zoneSensor.getId())
            );
        }
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}