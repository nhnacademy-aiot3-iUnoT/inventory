package com.nhnacademy.inventory.organizations.storage.service;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.dto.StorageCreateRequest;
import com.nhnacademy.inventory.organizations.storage.dto.StorageInfoResponse;
import com.nhnacademy.inventory.organizations.storage.dto.StorageStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.storage.dto.StorageUpdateRequest;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private StorageRepository storageRepository;
    @Mock
    private OrganizationMemberRepository memberRepository;

    @InjectMocks
    private StorageService storageService;

    private Organization organization;
    private OrganizationMember approvedMember;
    private Storage storage;

    @BeforeEach
    void setUp() throws Exception {
        organization = TestFixtures.createOrganization("테스트 조직1", "0123456789");
        setId(organization, 1L);

        approvedMember = TestFixtures.createOrganizationMember(organization);
        setId(approvedMember, 11L);

        storage = TestFixtures.createStorage(organization, "테스트 저장소1");
        setId(storage, 111L);
    }

    @Test
    @DisplayName("저장소 정상 생성 테스트")
    void createStorage() {
        StorageCreateRequest request = new StorageCreateRequest("테스트 저장소2", "테스트 설명");

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid())).willReturn(Optional.of(approvedMember));
        given(storageRepository.save(any(Storage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(storageRepository.existsByOrganizationAndNameAndStatusNot(organization, request.name(), StorageStatus.CLOSED))
                .willReturn(false);

        StorageInfoResponse response = storageService.createStorage(
                organization.getId(),
                approvedMember.getAccountUuid(),
                request
        );

        assertAll(
                () -> assertEquals(1L, response.organizationId()),
                () -> assertEquals("테스트 저장소2", response.name()),
                () -> assertEquals("테스트 설명", response.description()),
                () -> assertEquals(StorageStatus.ACTIVE, response.status())
        );

        verify(storageRepository).save(any(Storage.class));
        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).existsByOrganizationAndNameAndStatusNot(any(), anyString(), any());
    }

    @Test
    @DisplayName("정상 조회 테스트")
    void getStorages() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid())).willReturn(Optional.of(approvedMember));
        given(storageRepository.findAllByOrganizationAndStatusNot(organization, StorageStatus.CLOSED))
                .willReturn(List.of(storage));

        List<StorageInfoResponse> responses = storageService
                .getStorages(organization.getId(), approvedMember.getAccountUuid());

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(1L, responses.getFirst().organizationId()),
                () -> assertEquals("테스트 저장소1", responses.getFirst().name()),
                () -> assertEquals(StorageStatus.ACTIVE, responses.getFirst().status())
        );

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findAllByOrganizationAndStatusNot(any(), any());
    }

    @Test
    @DisplayName("정상 업데이트 테스트(이름, 설명)")
    void updateStorage() {
        StorageUpdateRequest request = new StorageUpdateRequest("업데이트된 이름", "업데이트된 설명");

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid())).willReturn(Optional.of(approvedMember));
        given(storageRepository.findByIdAndOrganization(storage.getId(), organization)).willReturn(Optional.of(storage));
        given(storageRepository.existsByOrganizationAndNameAndStatusNotAndIdNot(organization, request.name(), StorageStatus.CLOSED, storage.getId()))
                .willReturn(false);

        StorageInfoResponse response = storageService.updateStorage(
                organization.getId(), storage.getId(), approvedMember.getAccountUuid(),
                request
        );

        assertAll(
                () -> assertEquals(1L, response.organizationId()),
                () -> assertEquals("업데이트된 이름", response.name()),
                () -> assertEquals("업데이트된 설명", response.description()),
                () -> assertEquals(StorageStatus.ACTIVE, response.status())
        );

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findByIdAndOrganization(anyLong(), any());
        verify(storageRepository).existsByOrganizationAndNameAndStatusNotAndIdNot(any(), anyString(), any(), anyLong());
    }

    @Test
    @DisplayName("정상 업데이트 테스트(상태)")
    void updateStorageStatus() {
        StorageStatusUpdateRequest request = new StorageStatusUpdateRequest(StorageStatus.INACTIVE);

        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid())).willReturn(Optional.of(approvedMember));
        given(storageRepository.findByIdAndOrganization(storage.getId(), organization)).willReturn(Optional.of(storage));

        StorageInfoResponse response = storageService.updateStorageStatus(
                organization.getId(), storage.getId(), approvedMember.getAccountUuid(),
                request
        );

        assertAll(
                () -> assertEquals(1L, response.organizationId()),
                () -> assertEquals("테스트 저장소1", response.name()),
                () -> assertEquals(StorageStatus.INACTIVE, response.status()),
                () -> assertEquals(StorageStatus.INACTIVE, storage.getStatus())
        );

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findByIdAndOrganization(anyLong(), any());
    }

    @Test
    @DisplayName("정상 삭제 테스트")
    void closeStorage() {
        given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid())).willReturn(Optional.of(approvedMember));
        given(storageRepository.findByIdAndOrganization(storage.getId(), organization)).willReturn(Optional.of(storage));

        storageService.closeStorage(organization.getId(), storage.getId(), approvedMember.getAccountUuid());

        assertEquals(StorageStatus.CLOSED, storage.getStatus());

        verify(memberRepository).findByAccountUuid(any());
        verify(storageRepository).findByIdAndOrganization(anyLong(), any());
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}