package com.nhnacademy.inventory.organizations.storage.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.department.service.DepartmentService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.dto.*;
import com.nhnacademy.inventory.organizations.storage.exception.StorageInactiveException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.storage.repository.StoragePermissionRepository;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {
    private final StorageRepository storageRepository;
    private final OrganizationMemberRepository memberRepository;
    private final DepartmentService departmentService;
    private final StorageDepartmentRepository storageDepartmentRepository;
    private final MemberDepartmentRepository memberDepartmentRepository;
    private final StoragePermissionRepository storagePermissionRepository;
    private final ZoneRepository zoneRepository;

    @Transactional
    public StorageDetailResponse createStorage(StorageCreateRequest request){
        Organization organization = validateOrganizationOwner();

        validateDuplicateStorageName(organization, request.name());

        Storage storage = Storage.builder()
                .organization(organization)
                .name(request.name())
                .description(request.description())
                .status(StorageStatus.ACTIVE)
                .build();

        Storage saved = storageRepository.save(storage);

        List<StorageDepartment> storageDepartments = request.departmentIds() == null ? Collections.emptyList() :
                request.departmentIds().stream()
                        .map(departmentId -> {
                            Department department = departmentService.getDepartmentById(departmentId, organization.getId());
                            return StorageDepartment.create(saved, department);
                        })
                        .toList();

        storageDepartmentRepository.saveAll(storageDepartments);

        return StorageDetailResponse.from(saved);
    }

    public List<StorageInfoResponse> getStorages(){
        Organization organization = validateOrganizationMember();

        List<Storage> storages = storageRepository.findAllByOrganizationAndStatusNot(
                organization, StorageStatus.CLOSED);

        return storages.stream()
                .map(StorageInfoResponse::from)
                .toList();
    }

/**
     * 조직의 저장소 ID 목록. 삭제된 저장소는 제외한다.
     */
    public List<Long> getStorageIds() {
        Organization organization = validateOrganizationMember();

        return storageRepository.findIdsByOrganizationIdAndStatusNot(
                organization.getId(), StorageStatus.CLOSED);
    }

    public List<StorageInfoResponse> searchStorages(String name) {
        Organization organization = validateOrganizationMember();

        List<Storage> storages = name == null || name.isBlank()
                ? storageRepository.findAllByOrganizationAndStatusNot(organization, StorageStatus.CLOSED)
                : storageRepository.findAllByOrganizationAndNameContainingIgnoreCaseAndStatusNot(organization, name, StorageStatus.CLOSED);

        return storages.stream().map(StorageInfoResponse::from).toList();
    }

    public StorageDetailResponse getStorage(Long storageId){
        Storage storage = findByIdAndValidateMember(storageId);

        return StorageDetailResponse.from(storage);
    }

    public List<StorageInfoResponse> getStoragesInbound(){


        OrganizationMember organizationMember = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);



        if(organizationMember.isBoss()){

            List<Storage> storages = storageRepository.findAllByOrganization(organizationMember.getOrganization());

            List<StorageInfoResponse> infoResponses = storages.stream()
                    .map(StorageInfoResponse::from
                    ).toList();


            log.info("boss - storages count : {}",infoResponses.size());

            return infoResponses;

        }

        List<MemberDepartment> memberDepartments = memberDepartmentRepository.findAllByOrganizationMember(organizationMember);

        // 부서-조직원 없음 조직원은 부서가 꼭 있어야 함.
        if(memberDepartments.isEmpty()){
            throw new ForbiddenException();
        }

        List<Long> departmentIds = memberDepartments.stream()
                .map(
                        md -> md.getDepartment().getId()
                ).toList();


        List<StorageDepartment> storageDepartments = storageDepartmentRepository.findAllByDepartmentIdIn(departmentIds);

        List<Storage> storages = storageDepartments.stream()
                .map(
                        StorageDepartment::getStorage
                )
                .distinct()
                .toList();

        List<StorageInfoResponse> infoResponses = storages.stream()
                .map(
                        StorageInfoResponse::from
                ).toList();


        log.info("owner,member - storages count : {}",infoResponses.size());


        return infoResponses;

    }



    @Transactional
    public StorageDetailResponse updateStorage(Long storageId, StorageUpdateRequest request){
        Storage storage = findByIdAndValidateOwner(storageId);

        validateDuplicateStorageName(storage.getOrganization(), request.name(), storage.getId());

        storage.updateInfo(request.name(), request.description());

        return StorageDetailResponse.from(storage);
    }

    @Transactional
    public StorageDetailResponse updateStorageStatus(Long storageId, StorageStatusUpdateRequest request){
        Storage storage = findByIdAndValidateOwner(storageId);

        storage.changeStatus(request.status());

        if(!storage.isActive()){
            resetZoneEnvStatus(storage);
        }

        return StorageDetailResponse.from(storage);
    }

    @Transactional
    public void closeStorage(Long storageId){
        Storage storage = findByIdAndValidateOwner(storageId);

        storage.close();

        resetZoneEnvStatus(storage);
    }

    // 저장소가 비활성이면 소속 구역의 환경 판정도 멈추므로, 남아있던 경고 상태를 되돌린다.
    private void resetZoneEnvStatus(Storage storage){
        List<Zone> zones = zoneRepository.findAllByStorageId(storage.getId());

        zones.forEach(zone -> zone.changeEnvStatus(EnvStatus.NORMAL));
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

    public Storage validateOwnerAndGetStorage(Long storageId){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(StorageNotFoundException::new);

        if(!Objects.equals(member.getOrganization().getId(), storage.getOrganization().getId()) ||
            member.getOrganizationRole() == OrganizationRole.ORG_MEMBER){
            throw new ForbiddenException();
        }

        return storage;
    }

    private Organization validateOrganizationMember(){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        return member.getOrganization();
    }

    private Organization validateOrganizationOwner(){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        if(member.getOrganizationRole() == OrganizationRole.ORG_MEMBER){
            throw new ForbiddenException();
        }

        return member.getOrganization();
    }

    private Storage findByIdAndValidateOwner(Long storageId){
        Organization organization = validateOrganizationOwner();

        return storageRepository.findByIdAndOrganization(storageId, organization)
                .orElseThrow(StorageNotFoundException::new);
    }

    private Storage findByIdAndValidateMember(Long storageId){
        Organization organization = validateOrganizationMember();

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

    public void checkStoragePermission(Long storageId){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        if(member.getOrganizationRole() == OrganizationRole.ORG_BOSS ||
                member.getOrganizationRole() == OrganizationRole.ORG_OWNER){
            return;
        }

        boolean hasPermission = storagePermissionRepository.hasStoragePermission(
                UserContext.getUserUuid(), storageId
        );

        if(!hasPermission){
            throw new ForbiddenException();
        }
    }

    public List<Long> getAccessibleStorageIds(OrganizationMember member){
        if(member.getOrganizationRole() == OrganizationRole.ORG_BOSS ||
                member.getOrganizationRole() == OrganizationRole.ORG_OWNER){
            return storageRepository.findActiveIdsByOrganizationId(member.getOrganization().getId());
        }
        return storageRepository.findActiveIdsByOrganizationMemberId(member.getId());
    }

    public void validateStorageStatus(Storage storage){
        if (storage.getStatus() != StorageStatus.ACTIVE){
            throw new StorageInactiveException();
        }
    }
}
