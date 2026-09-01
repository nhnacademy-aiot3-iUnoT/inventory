package com.nhnacademy.inventory.organizations.notification.service;

import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannel;
import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannelPreference;
import com.nhnacademy.inventory.organizations.notification.domain.NotificationScopePreference;
import com.nhnacademy.inventory.organizations.notification.dto.reponse.NotificationPreferenceResponse;
import com.nhnacademy.inventory.organizations.notification.repository.NotificationChannelPreferenceRepository;
import com.nhnacademy.inventory.organizations.notification.repository.NotificationScopePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationReceiverService {
    private final NotificationScopePreferenceRepository scopePreferenceRepository;
    private final NotificationChannelPreferenceRepository channelPreferenceRepository;

    public List<NotificationPreferenceResponse> getReceiversForRuleEngine(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        List<NotificationScopePreference> enabledScopes =
                scopePreferenceRepository.findEnabledScopes(
                        organizationId,
                        storageId,
                        zoneId
                );

        if (enabledScopes.isEmpty()) {
            return List.of();
        }

        List<Long> organizationMemberIds = enabledScopes.stream()
                .map(scope -> scope.getOrganizationMember().getId())
                .toList();

        List<NotificationChannelPreference> enabledChannels =
                channelPreferenceRepository
                        .findAllByOrganizationMemberIdInAndIsEnabledTrueAndRecipientIsNotNullAndChannelIn(
                                organizationMemberIds,
                                Arrays.asList(NotificationChannel.values())
                        );

        return enabledChannels.stream()
                .filter(channelPreference -> !channelPreference.getRecipient().isBlank())
                .map(channelPreference -> new NotificationPreferenceResponse(
                        channelPreference.getOrganizationMember().getId(),
                        organizationId,
                        storageId,
                        zoneId,
                        channelPreference.getChannel(),
                        true,
                        channelPreference.getRecipient()
                ))
                .toList();
        }
    }

