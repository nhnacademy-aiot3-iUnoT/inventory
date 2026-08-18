package com.nhnacademy.inventory.organizations.zone.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
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

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final OrganizationMemberRepository memberRepository;
    private final StorageService storageService;

    @Transactional
    public ZoneDetailResponse createZone(Long storageId, ZoneCreateRequest request){
        Storage storage = storageService.validateOwnerAndGetStorage(storageId);

        validateDuplicateZoneName(storage, request.name());

        Zone zone = Zone.builder()
                .storage(storage)
                .name(request.name())
                .description(request.description())
                .status(ZoneStatus.ACTIVE)
                .envStatus(EnvStatus.NORMAL)
                .build();

        Zone saved = zoneRepository.save(zone);

        return ZoneDetailResponse.from(saved);
    }

    public List<ZoneInfoResponse> getZones(Long storageId){
        Storage storage = storageService.validateMemberAndGetStorage(storageId);

        List<Zone> zones = zoneRepository.findAllByStorageAndStatusNot(storage, ZoneStatus.CLOSED);

        return zones.stream()
                .map(ZoneInfoResponse::from)
                .toList();
    }

    public ZoneDetailResponse getZone(Long storageId, Long zoneId){
        Zone zone = findByIdAndValidateMember(storageId, zoneId);

        return ZoneDetailResponse.from(zone);
    }

    @Transactional
    public ZoneDetailResponse updateZone(Long storageId, Long zoneId, ZoneUpdateRequest request){
        Zone zone = findByIdAndValidateOwner(storageId, zoneId);

        validateDuplicateZoneName(zone.getStorage(), request.name(), zone.getId());

        zone.updateInfo(request.name(), request.description());

        return ZoneDetailResponse.from(zone);
    }

    @Transactional
    public ZoneDetailResponse updateZoneStatus(Long storageId, Long zoneId, ZoneStatusUpdateRequest request){
        Zone zone = findByIdAndValidateOwner(storageId, zoneId);

        zone.changeStatus(request.status());

        return ZoneDetailResponse.from(zone);
    }

    @Transactional
    public ZoneDetailResponse updateZoneEnvStatus(Long storageId, Long zoneId, ZoneEnvStatusUpdateRequest request){
        Zone zone = findByIdAndValidateOwner(storageId, zoneId);

        zone.changeEnvStatus(request.envStatus());

        return ZoneDetailResponse.from(zone);
    }

    @Transactional
    public void internalUpdateEnvStatus(Long zoneId, EnvStatus envStatus){
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(ZoneNotFoundException::new);

        zone.changeEnvStatus(envStatus);
    }

    @Transactional
    public void closeZone(Long storageId, Long zoneId){
        Zone zone = findByIdAndValidateOwner(storageId, zoneId);

        zone.close();
    }

    public Zone validateMemberAndGetZone(Long zoneId){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(ZoneNotFoundException::new);

        if (!Objects.equals(member.getOrganization().getId(), zone.getStorage().getOrganization().getId())){
            throw new ForbiddenException();
        }

        return zone;
    }

    public Zone validateOwnerAndGetZone(Long zoneId){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(ZoneNotFoundException::new);

        if (!Objects.equals(member.getOrganization().getId(), zone.getStorage().getOrganization().getId()) ||
            member.getOrganizationRole() == OrganizationRole.ORG_MEMBER){
            throw new ForbiddenException();
        }

        return zone;
    }

    private Zone findByIdAndValidateOwner(Long storageId, Long zoneId){
        Storage storage = storageService.validateOwnerAndGetStorage(storageId);

        return zoneRepository.findByIdAndStorage(zoneId, storage)
                .orElseThrow(ZoneNotFoundException::new);
    }

    private Zone findByIdAndValidateMember(Long storageId, Long zoneId){
        Storage storage = storageService.validateMemberAndGetStorage(storageId);

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
