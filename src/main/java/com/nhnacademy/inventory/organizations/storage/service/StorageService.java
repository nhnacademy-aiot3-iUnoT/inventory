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
import com.nhnacademy.inventory.organizations.storage.exception.StorageForbiddenException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {
    private final StorageRepository storageRepository;
    private final OrganizationMemberRepository memberRepository;

    @Transactional
    public StorageInfoResponse createStorage(Long organizationId, UUID accountUuid, StorageCreateRequest request){
        Organization organization = validateOrganizationMember(organizationId, accountUuid);

        validateDuplicateStorageName(organization, request.name());

        Storage storage = Storage.builder()
                .organization(organization)
                .name(request.name())
                .description(request.description())
                .status(StorageStatus.ACTIVE)
                .build();

        Storage saved = storageRepository.save(storage);
        return StorageInfoResponse.from(saved);
    }

    public List<StorageInfoResponse> getStorages(Long organizationId, UUID accountUuid){
        Organization organization = validateOrganizationMember(organizationId, accountUuid);

        List<Storage> storages = storageRepository.findAllByOrganizationAndStatusNot(organization, StorageStatus.CLOSED);

        return storages.stream()
                .map(StorageInfoResponse::from)
                .toList();
    }

    @Transactional
    public StorageInfoResponse updateStorage(Long organizationId, Long storageId, UUID accountUuid, StorageUpdateRequest request){
        Storage storage = findByIdAndValidate(organizationId, storageId, accountUuid);

        validateDuplicateStorageName(storage.getOrganization(), request.name(), storage.getId());

        storage.updateInfo(request.name(), request.description());

        return StorageInfoResponse.from(storage);
    }

    @Transactional
    public StorageInfoResponse updateStorageStatus(Long organizationId, Long storageId, UUID accountUuid, StorageStatusUpdateRequest request){
        Storage storage = findByIdAndValidate(organizationId, storageId, accountUuid);

        storage.changeStatus(request.status());

        return StorageInfoResponse.from(storage);
    }

    @Transactional
    public void closeStorage(Long organizationId, Long storageId, UUID accountUuid){
        Storage storage = findByIdAndValidate(organizationId, storageId, accountUuid);

        storage.close();
    }

    private Organization validateOrganizationMember(Long organizationId, UUID accountUuid){
        OrganizationMember member = memberRepository.findByAccountUuid(accountUuid)
                .orElseThrow(StorageForbiddenException::new);

        if(!Objects.equals(member.getOrganization().getId(), organizationId)){
            throw new StorageForbiddenException();
        }

        return member.getOrganization();
    }

    private Storage findByIdAndValidate(Long organizationId, Long storageId, UUID accountUuid){
        Organization organization = validateOrganizationMember(organizationId, accountUuid);

        return storageRepository.findByIdAndOrganization(storageId, organization)
                .orElseThrow(StorageNotFoundException::new);
    }

    private void validateDuplicateStorageName(Organization organization, String name, Long storageId){
        boolean exists = storageRepository.existsByOrganizationAndNameAndStatusNotAndIdNot(
                organization, name, StorageStatus.CLOSED, storageId
        );

        if (exists) {
            throw new StorageNameAlreadyExistsException();
        }
    }

    private void validateDuplicateStorageName(Organization organization, String name){
        boolean exists = storageRepository.existsByOrganizationAndNameAndStatusNot(
                organization, name, StorageStatus.CLOSED
        );

        if (exists) {
            throw new StorageNameAlreadyExistsException();
        }
    }
}
