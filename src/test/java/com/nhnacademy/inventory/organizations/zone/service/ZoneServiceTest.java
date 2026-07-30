package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
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
    private OrganizationMemberRepository memberRepository;
    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private ZoneService zoneService;

    private Organization organization;
    private Organization otherOrganization;
    private OrganizationMember approvedMember;
    private OrganizationMember otherMember;
    private Storage storage;
    private Zone zone;

    @BeforeEach
    void setUp() throws Exception {
        organization = TestFixtures.createOrganization("테스트 조직1", "0123456789");
        setId(organization, 1L);

        otherOrganization = TestFixtures.createOrganization("테스트 조직2", "1234567890");
        setId(otherOrganization, 2L);

        approvedMember = TestFixtures.createOrganizationMember(organization);
        setId(approvedMember, 11L);

        otherMember = TestFixtures.createOrganizationMember(otherOrganization);
        setId(otherMember, 22L);

        storage = TestFixtures.createStorage(organization, "테스트 저장소1");
        setId(storage, 111L);

        zone = TestFixtures.createZone(storage, "테스트 구역1");
        setId(zone, 1111L);
    }

    @Test
    @DisplayName("구역 생성 성공 테스트")
    void createZone_Success() {
        ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.existsByStorageAndNameAndStatusNot(storage, request.name(), ZoneStatus.CLOSED))
                .willReturn(false);
        given(zoneRepository.save(any(Zone.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ZoneInfoResponse response = zoneService.createZone(
                storage.getId(),
                approvedMember.getAccountUuid(),
                request
        );

        assertAll(
                () -> assertEquals(111L, response.storageId()),
                () -> assertEquals("테스트 구역2", response.name()),
                () -> assertEquals("테스트 설명", response.description()),
                () -> assertEquals(ZoneStatus.ACTIVE, response.status()),
                () -> assertEquals(EnvStatus.NORMAL, response.envStatus())
        );

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findById(anyLong());
        verify(zoneRepository).existsByStorageAndNameAndStatusNot(any(), anyString(), any());
        verify(zoneRepository).save(any());
    }

    @Test
    @DisplayName("구역 생성 실패 - 권한없음(멤버 아님)")
    void createZone_Fail_NotMember() {
        ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
            zoneService.createZone(
                    storage.getId(),
                    UUID.randomUUID(),
                    request
            )
        );

        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("구역 생성 실패 - 권한없음(조직 아이디 불일치)")
    void createZone_Fail_IdMismatch() {
        ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));

        assertThrowsExactly(ForbiddenException.class, () ->
            zoneService.createZone(
                    storage.getId(),
                    otherMember.getAccountUuid(),
                    request
            )
        );

        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("구역 생성 실패 - 존재하지 않는 저장소")
    void createZone_Fail_NotStorage() {
        ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        assertThrowsExactly(StorageNotFoundException.class, () ->
                zoneService.createZone(
                        333L,
                        approvedMember.getAccountUuid(),
                        request
                )
        );

        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("구역 생성 실패 - 중복 이름")
    void createZone_Fail_DuplicateName() {
        ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역2", "테스트 설명");

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.existsByStorageAndNameAndStatusNot(storage, request.name(), ZoneStatus.CLOSED))
                .willReturn(true);

        assertThrowsExactly(ZoneNameAlreadyExistsException.class, () ->
                zoneService.createZone(
                        storage.getId(),
                        approvedMember.getAccountUuid(),
                        request
                )
        );

        verify(zoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("구역 조회 성공 테스트")
    void getZones_Success() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findAllByStorageAndStatusNot(storage, ZoneStatus.CLOSED))
                .willReturn(List.of(zone));

        List<ZoneInfoResponse> responses = zoneService.getZones(
                storage.getId(),
                approvedMember.getAccountUuid()
        );

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(111L, responses.getFirst().storageId()),
                () -> assertEquals("테스트 구역1", responses.getFirst().name())
        );

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findById(anyLong());
        verify(zoneRepository).findAllByStorageAndStatusNot(any(), any());
    }

    @Test
    @DisplayName("구역 조회 성공 테스트 (빈 리스트)")
    void getZones_Success_empty() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findAllByStorageAndStatusNot(storage, ZoneStatus.CLOSED))
                .willReturn(List.of());

        List<ZoneInfoResponse> responses = zoneService.getZones(
                storage.getId(),
                approvedMember.getAccountUuid()
        );

        assertAll(
                () -> assertEquals(0, responses.size())
        );
    }

    @Test
    @DisplayName("구역 조회 실패 - 권한없음(멤버 아님)")
    void getZones_Fail_NotMember() {
        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.getZones(
                        storage.getId(),
                        UUID.randomUUID()
                )
        );
    }

    @Test
    @DisplayName("구역 조회 실패 - 권한없음(조직아이디 불일치)")
    void getZones_Fail_IdMismatch() {
        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.getZones(
                        storage.getId(),
                        otherMember.getAccountUuid()
                )
        );
    }

    @Test
    @DisplayName("구역 조회 실패 - 존재하지 않는 저장소")
    void getZones_Fail_NotStorage() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        assertThrowsExactly(StorageNotFoundException.class, () ->
                zoneService.getZones(
                        333L,
                        approvedMember.getAccountUuid()
                )
        );
    }

    @Test
    @DisplayName("구역 정보 업데이트 성공 테스트")
    void updateZone_Success() {
        ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                .willReturn(Optional.of(zone));
        given(zoneRepository.existsByStorageAndNameAndStatusNotAndIdNot(storage, request.name(), ZoneStatus.CLOSED, zone.getId()))
                .willReturn(false);

        ZoneInfoResponse response = zoneService.updateZone(storage.getId(), zone.getId(), approvedMember.getAccountUuid(), request);

        assertAll(
                () -> assertEquals(1111L, response.zoneId()),
                () -> assertEquals(111L, response.storageId()),
                () -> assertEquals("업데이트 구역", response.name()),
                () -> assertEquals("업데이트 설명", response.description())
        );

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findById(anyLong());
        verify(zoneRepository).findByIdAndStorage(anyLong(), any());
        verify(zoneRepository).existsByStorageAndNameAndStatusNotAndIdNot(any(), anyString(), any(), anyLong());
    }

    @Test
    @DisplayName("구역 정보 업데이트 실패 - 권한 없음(멤버 아님)")
    void updateZone_Fail_NotMember() {
        ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.updateZone(storage.getId(), zone.getId(), UUID.randomUUID(), request)
        );
    }

    @Test
    @DisplayName("구역 정보 업데이트 실패 - 권한 없음(조직아이디 불일치)")
    void updateZone_Fail_IdMismatch() {
        ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.updateZone(storage.getId(), zone.getId(), otherMember.getAccountUuid(), request)
        );
    }

    @Test
    @DisplayName("구역 정보 업데이트 실패 - 존 없음")
    void updateZone_Fail_NotFoundZone() {
        ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(anyLong(), any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ZoneNotFoundException.class, () ->
                zoneService.updateZone(storage.getId(), 3333L, approvedMember.getAccountUuid(), request)
        );
    }

    @Test
    @DisplayName("구역 정보 업데이트 실패 - 이름 중복")
    void updateZone_Fail_DuplicateName() {
        ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                .willReturn(Optional.of(zone));
        given(zoneRepository.existsByStorageAndNameAndStatusNotAndIdNot(storage, request.name(), ZoneStatus.CLOSED, zone.getId()))
                .willReturn(true);

        assertThrowsExactly(ZoneNameAlreadyExistsException.class, () ->
                zoneService.updateZone(storage.getId(), zone.getId(), approvedMember.getAccountUuid(), request)
        );
    }

    @Test
    @DisplayName("구역 상태 업데이트 성공 테스트")
    void updateZoneStatus_Success() {
        ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                .willReturn(Optional.of(zone));

        ZoneInfoResponse response = zoneService.updateZoneStatus(storage.getId(), zone.getId(), approvedMember.getAccountUuid(), request);

        assertAll(
                () -> assertEquals(1111L, response.zoneId()),
                () -> assertEquals(111L, response.storageId()),
                () -> assertEquals("테스트 구역1", response.name()),
                () -> assertEquals(ZoneStatus.INACTIVE, response.status())
        );

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findById(anyLong());
        verify(zoneRepository).findByIdAndStorage(anyLong(), any());
    }

    @Test
    @DisplayName("구역 상태 업데이트 실패 - 권한없음(멤버 아님)")
    void updateZoneStatus_Fail_NotMember() {
        ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.updateZoneStatus(storage.getId(), zone.getId(), UUID.randomUUID(), request)
        );
    }

    @Test
    @DisplayName("구역 상태 업데이트 실패 - 권한없음(아이디 불일치)")
    void updateZoneStatus_Fail_IdMismatch() {
        ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.updateZoneStatus(storage.getId(), zone.getId(), otherMember.getAccountUuid(), request)
        );
    }

    @Test
    @DisplayName("구역 상태 업데이트 실패 - 존 없음")
    void updateZoneStatus_Fail_NotFoundZone() {
        ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(anyLong(), any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ZoneNotFoundException.class, () ->
                zoneService.updateZoneStatus(storage.getId(), 3333L, approvedMember.getAccountUuid(), request)
        );
    }

    @Test
    @DisplayName("구역 환경상태 업데이트 성공 테스트")
    void updateZoneEnvStatus() {
        ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                .willReturn(Optional.of(zone));

        ZoneInfoResponse response = zoneService.updateZoneEnvStatus(storage.getId(), zone.getId(), approvedMember.getAccountUuid(), request);

        assertAll(
                () -> assertEquals(1111L, response.zoneId()),
                () -> assertEquals(111L, response.storageId()),
                () -> assertEquals("테스트 구역1", response.name()),
                () -> assertEquals(EnvStatus.CRITICAL, response.envStatus())
        );

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findById(anyLong());
        verify(zoneRepository).findByIdAndStorage(anyLong(), any());
    }

    @Test
    @DisplayName("구역 환경상태 업데이트 실패 - 권한없음(멤버 아님)")
    void updateZoneEnvStatus_Fail_NotMember() {
        ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.updateZoneEnvStatus(storage.getId(), zone.getId(), UUID.randomUUID(), request)
        );
    }

    @Test
    @DisplayName("구역 환경상태 업데이트 실패 - 권한없음(조직 아이디 불일치)")
    void updateZoneEnvStatus_Fail_IdMismatch() {
        ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.updateZoneEnvStatus(storage.getId(), zone.getId(), otherMember.getAccountUuid(), request)
        );
    }

    @Test
    @DisplayName("구역 환경상태 업데이트 실패 - 존 없음")
    void updateZoneEnvStatus_Fail_NotFoundZone() {
        ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(anyLong(), any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ZoneNotFoundException.class, () ->
                zoneService.updateZoneEnvStatus(storage.getId(), 3333L, approvedMember.getAccountUuid(), request)
        );
    }

    @Test
    @DisplayName("구역 삭제 성공 테스트")
    void closeZone() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(zone.getId(), storage))
                .willReturn(Optional.of(zone));

        assertDoesNotThrow(() ->
                zoneService.closeZone(storage.getId(), zone.getId(), approvedMember.getAccountUuid())
        );

        assertEquals(ZoneStatus.CLOSED, zone.getStatus());

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findById(anyLong());
        verify(zoneRepository).findByIdAndStorage(anyLong(), any());
    }

    @Test
    @DisplayName("구역 삭제 실패 - 권한없음(멤버 아님)")
    void closeZone_Fail_NotMember() {
        given(memberRepository.findByAccountUuid(any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.closeZone(storage.getId(), zone.getId(), UUID.randomUUID())
        );
    }

    @Test
    @DisplayName("구역 삭제 실패 - 권한없음(조직 아이디 불일치)")
    void closeZone_Fail_IdMismatch() {
        given(memberRepository.findByAccountUuid(otherMember.getAccountUuid()))
                .willReturn(Optional.of(otherMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));

        assertThrowsExactly(ForbiddenException.class, () ->
                zoneService.closeZone(storage.getId(), zone.getId(), otherMember.getAccountUuid())
        );
    }

    @Test
    @DisplayName("구역 삭제 실패 - 존 없음")
    void closeZone_Fail_NotFoundZone() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                .willReturn(Optional.of(approvedMember));
        given(storageRepository.findById(storage.getId()))
                .willReturn(Optional.of(storage));
        given(zoneRepository.findByIdAndStorage(anyLong(), any()))
                .willReturn(Optional.empty());

        assertThrowsExactly(ZoneNotFoundException.class, () ->
                zoneService.closeZone(storage.getId(), 3333L, approvedMember.getAccountUuid())
        );
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}