package com.nhnacademy.inventory.organizations.notification.repository;

import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannel;
import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannelPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotificationChannelPreferenceRepository extends JpaRepository<NotificationChannelPreference, Long> {

    List<NotificationChannelPreference> findAllByOrganizationMemberId(Long organizationMemberId);

    Optional<NotificationChannelPreference> findByOrganizationMemberIdAndChannel(
            Long organizationMemberId,
            NotificationChannel channel
    );

    List<NotificationChannelPreference> findAllByOrganizationMemberIdInAndIsEnabledTrueAndRecipientIsNotNullAndChannelIn(
            Collection<Long> organizationMemberIds,
            Collection<NotificationChannel> channels
    );
}
