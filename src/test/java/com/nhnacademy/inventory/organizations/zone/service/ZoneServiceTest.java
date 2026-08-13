package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import com.nhnacademy.inventory.organizations.zone.dto.*;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private ZoneService zoneService;

    private Organization organization;
    private OrganizationMember approvedMember;
    private Storage storage;
    private Zone zone;

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
    }

    @Nested
    @DisplayName("구역 생성 테스트")
    class createZone{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);

            given(zoneRepository.existsByStorageAndNameAndStatusNot(storage, request.name(), ZoneStatus.CLOSED))
                    .willReturn(false);
            given(zoneRepository.save(any(Zone.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            ZoneInfoResponse response = zoneService.createZone(
                    storage.getId(),
                    request
            );

            assertAll(
                    () -> assertEquals(111L, response.storageId()),
                    () -> assertEquals("테스트 구역2", response.name()),
                    () -> assertEquals("테스트 설명", response.description()),
                    () -> assertEquals(ZoneStatus.ACTIVE, response.status()),
                    () -> assertEquals(EnvStatus.NORMAL, response.envStatus())
            );

            verify(storageService).validateOwnerAndGetStorage(anyLong());
            verify(zoneRepository).existsByStorageAndNameAndStatusNot(any(), anyString(), any());
            verify(zoneRepository).save(any());
        }

        @Test
        @DisplayName("실패 - 권한없음(멤버 아님)")
        void fail_Forbidden() {
            ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneService.createZone(
                            storage.getId(),
                            request
                    )
            );

            verify(zoneRepository, never()).save(any());
        }


        @Test
        @DisplayName("실패 - 존재하지 않는 저장소")
        void fail_NotFoundStorage() {
            ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

            given(storageService.validateOwnerAndGetStorage(anyLong()))
                    .willThrow(new StorageNotFoundException());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(StorageNotFoundException.class, () ->
                    zoneService.createZone(
                            333L,
                            request
                    )
            );

            verify(zoneRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 중복 이름")
        void fail_DuplicateName() {
            ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.existsByStorageAndNameAndStatusNot(storage, request.name(), ZoneStatus.CLOSED))
                    .willReturn(true);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ZoneNameAlreadyExistsException.class, () ->
                    zoneService.createZone(
                            storage.getId(),
                            request
                    )
            );

            verify(zoneRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("구역 조회 테스트")
    class getZones{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(storageService.validateMemberAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findAllByStorageAndStatusNot(storage, ZoneStatus.CLOSED))
                    .willReturn(List.of(zone));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            List<ZoneInfoResponse> responses = zoneService.getZones(
                    storage.getId()
            );

            assertAll(
                    () -> assertEquals(1, responses.size()),
                    () -> assertEquals(111L, responses.getFirst().storageId()),
                    () -> assertEquals("테스트 구역1", responses.getFirst().name())
            );

            verify(storageService).validateMemberAndGetStorage(anyLong());
            verify(zoneRepository).findAllByStorageAndStatusNot(any(), any());
        }

        @Test
        @DisplayName("성공 테스트 (빈 리스트)")
        void success_empty() {
            given(storageService.validateMemberAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findAllByStorageAndStatusNot(storage, ZoneStatus.CLOSED))
                    .willReturn(List.of());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            List<ZoneInfoResponse> responses = zoneService.getZones(
                    storage.getId()
            );

            assertAll(
                    () -> assertEquals(0, responses.size())
            );
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            given(storageService.validateMemberAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneService.getZones(
                            storage.getId()
                    )
            );
        }


        @Test
        @DisplayName("실패 - 존재하지 않는 저장소")
        void fail_NotFoundStorage() {
            given(storageService.validateMemberAndGetStorage(anyLong()))
                    .willThrow(new StorageNotFoundException());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(StorageNotFoundException.class, () ->
                    zoneService.getZones(
                            333L
                    )
            );
        }
    }

    @Nested
    @DisplayName("구역 정보 업데이트 테스트")
    class updateZone{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                    .willReturn(Optional.of(zone));
            given(zoneRepository.existsByStorageAndNameAndStatusNotAndIdNot(storage, request.name(), ZoneStatus.CLOSED, zone.getId()))
                    .willReturn(false);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            ZoneInfoResponse response = zoneService.updateZone(storage.getId(), zone.getId(),  request);

            assertAll(
                    () -> assertEquals(1111L, response.zoneId()),
                    () -> assertEquals(111L, response.storageId()),
                    () -> assertEquals("업데이트 구역", response.name()),
                    () -> assertEquals("업데이트 설명", response.description())
            );

            verify(storageService).validateOwnerAndGetStorage(anyLong());
            verify(zoneRepository).findByIdAndStorage(anyLong(), any());
            verify(zoneRepository).existsByStorageAndNameAndStatusNotAndIdNot(any(), anyString(), any(), anyLong());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneService.updateZone(storage.getId(), zone.getId(), request)
            );
        }

        @Test
        @DisplayName("실패 - 존 없음")
        void fail_NotFoundZone() {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(anyLong(), any()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ZoneNotFoundException.class, () ->
                    zoneService.updateZone(storage.getId(), 3333L, request)
            );
        }

        @Test
        @DisplayName("실패 - 이름 중복")
        void fail_DuplicateName() {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                    .willReturn(Optional.of(zone));
            given(zoneRepository.existsByStorageAndNameAndStatusNotAndIdNot(storage, request.name(), ZoneStatus.CLOSED, zone.getId()))
                    .willReturn(true);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ZoneNameAlreadyExistsException.class, () ->
                    zoneService.updateZone(storage.getId(), zone.getId(), request)
            );
        }
    }

    @Nested
    @DisplayName("구역 상태 업데이트 테스트")
    class updateZoneStatus{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                    .willReturn(Optional.of(zone));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            ZoneInfoResponse response = zoneService.updateZoneStatus(storage.getId(), zone.getId(), request);

            assertAll(
                    () -> assertEquals(1111L, response.zoneId()),
                    () -> assertEquals(111L, response.storageId()),
                    () -> assertEquals("테스트 구역1", response.name()),
                    () -> assertEquals(ZoneStatus.INACTIVE, response.status())
            );

            verify(storageService).validateOwnerAndGetStorage(anyLong());
            verify(zoneRepository).findByIdAndStorage(anyLong(), any());
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneService.updateZoneStatus(storage.getId(), zone.getId(), request)
            );
        }

        @Test
        @DisplayName("실패 - 존 없음")
        void fail_NotFoundZone() {
            ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(anyLong(), any()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ZoneNotFoundException.class, () ->
                    zoneService.updateZoneStatus(storage.getId(), 3333L, request)
            );
        }
    }

    @Nested
    @DisplayName("구역 환경상태 업데이트 테스트")
    class updateZoneEnvStatus{

        @Test
        @DisplayName("성공 테스트")
        void Success() {
            ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                    .willReturn(Optional.of(zone));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            ZoneInfoResponse response = zoneService.updateZoneEnvStatus(storage.getId(), zone.getId(), request);

            assertAll(
                    () -> assertEquals(1111L, response.zoneId()),
                    () -> assertEquals(111L, response.storageId()),
                    () -> assertEquals("테스트 구역1", response.name()),
                    () -> assertEquals(EnvStatus.CRITICAL, response.envStatus())
            );

            verify(storageService).validateOwnerAndGetStorage(anyLong());
            verify(zoneRepository).findByIdAndStorage(anyLong(), any());
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneService.updateZoneEnvStatus(storage.getId(), zone.getId(), request)
            );
        }

        @Test
        @DisplayName("실패 - 존 없음")
        void fail_NotFoundZone() {
            ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(anyLong(), any()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ZoneNotFoundException.class, () ->
                    zoneService.updateZoneEnvStatus(storage.getId(), 3333L, request)
            );
        }
    }

    @Nested
    @DisplayName("구역 환경상태 업데이트(내부 api용 메서드) 테스트")
    class internalUpdateZoneEnvStatus{

        @Test
        @DisplayName("성공 테스트")
        void Success() {
            given(zoneRepository.findById(zone.getId()))
                    .willReturn(Optional.of(zone));

            assertNotEquals(EnvStatus.CRITICAL, zone.getEnvStatus());

            assertDoesNotThrow(() -> zoneService.internalUpdateEnvStatus(zone.getId(), EnvStatus.CRITICAL));

            assertEquals(EnvStatus.CRITICAL, zone.getEnvStatus());
        }

        @Test
        @DisplayName("실패 - 존 없음")
        void fail_NotFoundZone() {
            given(zoneRepository.findById(zone.getId()))
                    .willReturn(Optional.empty());

            assertThrowsExactly(ZoneNotFoundException.class, () ->
                    zoneService.internalUpdateEnvStatus(zone.getId(), EnvStatus.CRITICAL)
            );
        }
    }

    @Nested
    @DisplayName("구역 삭제 테스트")
    class deleteZone{
        @Test
        @DisplayName("성공 테스트")
        void Success() {
            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                    .willReturn(Optional.of(zone));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertDoesNotThrow(() ->
                    zoneService.closeZone(storage.getId(), zone.getId())
            );

            assertEquals(ZoneStatus.CLOSED, zone.getStatus());

            verify(storageService).validateOwnerAndGetStorage(anyLong());
            verify(zoneRepository).findByIdAndStorage(anyLong(), any());
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            UserContext.setUserUuid(UUID.randomUUID());

            assertThrowsExactly(ForbiddenException.class, () ->
                    zoneService.closeZone(storage.getId(), zone.getId())
            );
        }


        @Test
        @DisplayName("실패 - 존 없음")
        void fail_NotFoundZone() {
            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(zoneRepository.findByIdAndStorage(anyLong(), any()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ZoneNotFoundException.class, () ->
                    zoneService.closeZone(storage.getId(), 3333L)
            );
        }
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}