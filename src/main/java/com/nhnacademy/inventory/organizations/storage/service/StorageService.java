package com.nhnacademy.inventory.organizations.storage.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.dto.StorageCreateRequest;
import com.nhnacademy.inventory.organizations.storage.dto.StorageInfoResponse;
import com.nhnacademy.inventory.organizations.storage.dto.StorageStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.storage.dto.StorageUpdateRequest;
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
    public StorageInfoResponse createStorage(Long organizationId, StorageCreateRequest request){
        Organization organization = validateOrganizationMember(organizationId);

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

    public List<StorageInfoResponse> getStorages(Long organizationId){
        Organization organization = validateOrganizationMember(organizationId);

        List<Storage> storages = storageRepository.findAllByOrganizationAndStatusNot(organization, StorageStatus.CLOSED);

        return storages.stream()
                .map(StorageInfoResponse::from)
                .toList();
    }

    @Transactional
    public StorageInfoResponse updateStorage(Long organizationId, Long storageId, StorageUpdateRequest request){
        Storage storage = findByIdAndValidate(organizationId, storageId);

        validateDuplicateStorageName(storage.getOrganization(), request.name(), storage.getId());

        storage.updateInfo(request.name(), request.description());

        return StorageInfoResponse.from(storage);
    }

    @Transactional
    public StorageInfoResponse updateStorageStatus(Long organizationId, Long storageId, StorageStatusUpdateRequest request){
        Storage storage = findByIdAndValidate(organizationId, storageId);

        storage.changeStatus(request.status());

        return StorageInfoResponse.from(storage);
    }

    @Transactional
    public void closeStorage(Long organizationId, Long storageId){
        Storage storage = findByIdAndValidate(organizationId, storageId);

        storage.close();
    }

    public Storage validateMemberAndGetStorage(Long storageId){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(StorageNotFoundException::new);

        if(!Objects.equals(member.getOrganization().getId(), storage.getOrganization().getId())){
            throw new ForbiddenException();
        }

        return storage;
    }

    private Organization validateOrganizationMember(Long organizationId){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        if(!Objects.equals(member.getOrganization().getId(), organizationId)){
            throw new ForbiddenException();
        }

        return member.getOrganization();
    }

    private Storage findByIdAndValidate(Long organizationId, Long storageId){
        Organization organization = validateOrganizationMember(organizationId);

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
