package com.nhnacademy.inventory.organizations.notification.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannelPreference;
import com.nhnacademy.inventory.organizations.notification.domain.NotificationScopePreference;
import com.nhnacademy.inventory.organizations.notification.dto.NotificationScopeSource;
import com.nhnacademy.inventory.organizations.notification.dto.reponse.NotificationScopePreferenceEffectiveResponse;
import com.nhnacademy.inventory.organizations.notification.dto.request.NotificationChannelPreferencesUpdateRequest;
import com.nhnacademy.inventory.organizations.notification.dto.request.NotificationScopePreferenceRequest;
import com.nhnacademy.inventory.organizations.notification.exception.NotificationPreferenceAlreadyExistsException;
import com.nhnacademy.inventory.organizations.notification.exception.NotificationPreferenceInvalidScopeException;
import com.nhnacademy.inventory.organizations.notification.exception.NotificationPreferenceNotFoundException;
import com.nhnacademy.inventory.organizations.notification.exception.NotificationRecipientRequiredException;
import com.nhnacademy.inventory.organizations.notification.repository.NotificationChannelPreferenceRepository;
import com.nhnacademy.inventory.organizations.notification.repository.NotificationScopePreferenceRepository;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceService {
    private final NotificationScopePreferenceRepository scopePreferenceRepository;
    private final NotificationChannelPreferenceRepository channelPreferenceRepository;
    private final OrganizationAccessService organizationAccessService;
    private final StorageRepository storageRepository;
    private final ZoneRepository zoneRepository;


    @Transactional
    public void upsertScopePreference(
            Long organizationId,
            NotificationScopePreferenceRequest request
    ) {
        OrganizationMember member = getCurrentMemberOf(organizationId);
        Storage storage = resolveStorage(organizationId, request.storageId());
        Zone zone = resolveZone(storage, request.zoneId());

        NotificationScopePreference scopePreference = scopePreferenceRepository
                .findScope(member.getId(), request.storageId(), request.zoneId())
                .map(existing -> {
                    existing.updateScope(
                            storage,
                            zone,
                            enabledOrDefault(request.enabled())
                    );
                    return existing;
                })
                .orElseGet(() -> NotificationScopePreference.create(
                        member,
                        storage,
                        zone,
                        enabledOrDefault(request.enabled())
                ));

        scopePreferenceRepository.save(scopePreference);
    }

    public List<NotificationScopePreference> getScopePreferences(Long organizationId){
        OrganizationMember member = getCurrentMemberOf(organizationId);

        return scopePreferenceRepository.findAllByOrganizationMemberId(member.getId());
    }

    public NotificationScopePreferenceEffectiveResponse getEffectiveScopePreference(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        OrganizationMember member = getCurrentMemberOf(organizationId);

        Storage storage = resolveStorage(organizationId, storageId);
        resolveZone(storage, zoneId);

        return scopePreferenceRepository
                .findEffectiveScope(member.getId(), storageId, zoneId)
                .map(preference -> new NotificationScopePreferenceEffectiveResponse(
                        storageId,
                        zoneId,
                        preference.isEnabled(),
                        sourceOf(preference)
                ))
                .orElseGet(() -> new NotificationScopePreferenceEffectiveResponse(
                        storageId,
                        zoneId,
                        true,
                        NotificationScopeSource.DEFAULT
                ));
    }

    @Transactional
    public void deleteScopePreference(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        OrganizationMember member = getCurrentMemberOf(organizationId);

        Storage storage = resolveStorage(organizationId, storageId);
        resolveZone(storage, zoneId);

        NotificationScopePreference scopePreference = scopePreferenceRepository
                .findScope(member.getId(), storageId, zoneId)
                .orElseThrow(NotificationPreferenceNotFoundException::new);

        scopePreferenceRepository.delete(scopePreference);
    }

    public List<NotificationChannelPreference> getChannelPreferences(Long organizationId) {
        OrganizationMember member = getCurrentMemberOf(organizationId);

        return channelPreferenceRepository.findAllByOrganizationMemberId(member.getId());
    }

    @Transactional
    public void updateChannelPreferences(
            Long organizationId,
            NotificationChannelPreferencesUpdateRequest request
    ) {
        OrganizationMember member = getCurrentMemberOf(organizationId);

        for (NotificationChannelPreferencesUpdateRequest.NotificationChannelPreferenceUpdateItem item : request.channels()) {
            if (item.enabled() && isBlank(item.recipient())) {
                throw new NotificationRecipientRequiredException();
            }

            NotificationChannelPreference channelPreference = channelPreferenceRepository
                    .findByOrganizationMemberIdAndChannel(member.getId(), item.channel())
                    .map(existing -> {
                        existing.update(item.recipient(), item.enabled());
                        return existing;
                    })
                    .orElseGet(() -> NotificationChannelPreference.create(
                            member,
                            item.channel(),
                            item.recipient(),
                            item.enabled()
                    ));

            channelPreferenceRepository.save(channelPreference);
        }
    }


    private OrganizationMember getCurrentMemberOf(Long organizationId) {
        OrganizationMember member = organizationAccessService.getCurrentMember();

        if (!member.getOrganization().getId().equals(organizationId)) {
            throw new ForbiddenException();
        }

        return member;
    }

    private Storage resolveStorage(Long organizationId, Long storageId) {
        if (storageId == null) {
            return null;
        }

        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(StorageNotFoundException::new);

        if (!storage.getOrganization().getId().equals(organizationId)) {
            throw new StorageNotFoundException();
        }

        return storage;
    }

    private Zone resolveZone(Storage storage, Long zoneId) {
        if (zoneId == null) {
            return null;
        }

        if (storage == null) {
            throw new NotificationPreferenceInvalidScopeException();
        }

        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(ZoneNotFoundException::new);

        if (!zone.getStorage().getId().equals(storage.getId())) {
            throw new ZoneNotFoundException();
        }

        return zone;
    }

    private NotificationScopeSource sourceOf(NotificationScopePreference preference) {
        if (preference.getZone() != null) {
            return NotificationScopeSource.ZONE;
        }

        if (preference.getStorage() != null) {
            return NotificationScopeSource.STORAGE;
        }

        return NotificationScopeSource.ORGANIZATION;
    }


    private boolean enabledOrDefault(Boolean enabled) {
        return enabled == null || enabled;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
