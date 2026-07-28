package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
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
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final OrganizationMemberRepository memberRepository;
    private final StorageRepository storageRepository;

    @Transactional
    public ZoneInfoResponse createZone(Long storageId, UUID accountUuid, ZoneCreateRequest request){
        Storage storage = validateOrganizationMember(storageId, accountUuid);

        validateDuplicateZoneName(storage, request.name());

        Zone zone = Zone.builder()
                .storage(storage)
                .name(request.name())
                .description(request.description())
                .status(ZoneStatus.ACTIVE)
                .envStatus(EnvStatus.NORMAL)
                .build();

        Zone saved = zoneRepository.save(zone);

        return ZoneInfoResponse.from(saved);
    }

    public List<ZoneInfoResponse> getZones(Long storageId, UUID accountUuid){
        Storage storage = validateOrganizationMember(storageId, accountUuid);

        List<Zone> zones = zoneRepository.findAllByStorageAndStatusNot(storage, ZoneStatus.CLOSED);

        return zones.stream()
                .map(ZoneInfoResponse::from)
                .toList();
    }

    @Transactional
    public ZoneInfoResponse updateZone(Long storageId, Long zoneId, UUID accountUuid, ZoneUpdateRequest request){
        Zone zone = findByIdAndValidate(storageId, zoneId, accountUuid);

        validateDuplicateZoneName(zone.getStorage(), request.name(), zone.getId());

        zone.updateInfo(request.name(), request.description());

        return ZoneInfoResponse.from(zone);
    }

    @Transactional
    public ZoneInfoResponse updateZoneStatus(Long storageId, Long zoneId, UUID accountUuid, ZoneStatusUpdateRequest request){
        Zone zone = findByIdAndValidate(storageId, zoneId, accountUuid);

        zone.changeStatus(request.status());

        return ZoneInfoResponse.from(zone);
    }

    @Transactional
    public ZoneInfoResponse updateZoneEnvStatus(Long storageId, Long zoneId, UUID accountUuid, ZoneEnvStatusUpdateRequest request){
        Zone zone = findByIdAndValidate(storageId, zoneId, accountUuid);

        zone.changeEnvStatus(request.envStatus());

        return ZoneInfoResponse.from(zone);
    }

    @Transactional
    public void closeZone(Long storageId, Long zoneId, UUID accountUuid){
        Zone zone = findByIdAndValidate(storageId, zoneId, accountUuid);

        zone.close();
    }

    private Storage validateOrganizationMember(Long storageId, UUID accountUUid){
        OrganizationMember member = memberRepository.findByAccountUuid(accountUUid)
                .orElseThrow(ForbiddenException::new);

        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(StorageNotFoundException::new);

        if(!Objects.equals(member.getOrganization().getId(), storage.getOrganization().getId())){
            throw new ForbiddenException();
        }

        return storage;
    }

    private Zone findByIdAndValidate(Long storageId, Long zoneId, UUID accountUuid){
        Storage storage = validateOrganizationMember(storageId, accountUuid);

        return zoneRepository.findByIdAndStorage(zoneId, storage)
                .orElseThrow(ZoneNotFoundException::new);
    }

    private void validateDuplicateZoneName(Storage storage, String name, Long zoneId){
        boolean exists = zoneRepository.existsByStorageAndNameAndStatusNotAndIdNot(
                storage, name, ZoneStatus.CLOSED, zoneId
        );

        if (exists){
            throw new ZoneNameAlreadyExistsException();
        }
    }

    private void validateDuplicateZoneName(Storage storage, String name){
        boolean exists = zoneRepository.existsByStorageAndNameAndStatusNot(
                storage, name, ZoneStatus.CLOSED
        );

        if (exists){
            throw new ZoneNameAlreadyExistsException();
        }
    }
}
