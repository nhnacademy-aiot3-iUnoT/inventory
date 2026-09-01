package com.nhnacademy.inventory.organizations.notification.repository.impl;


import com.nhnacademy.inventory.organizations.notification.domain.NotificationScopePreference;
import com.nhnacademy.inventory.organizations.notification.repository.NotificationScopePreferenceRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nhnacademy.inventory.organizations.notification.domain.QNotificationScopePreference.notificationScopePreference;

@RequiredArgsConstructor
public class NotificationScopePreferenceRepositoryImpl implements NotificationScopePreferenceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NotificationScopePreference> findEnabledScopes(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        List<NotificationScopePreference> candidates = queryFactory
                .selectFrom(notificationScopePreference)
                .join(notificationScopePreference.organizationMember).fetchJoin()
                .leftJoin(notificationScopePreference.storage).fetchJoin()
                .leftJoin(notificationScopePreference.zone).fetchJoin()
                .where(
                        notificationScopePreference.organizationMember.organization.id.eq(organizationId),
                        matchingScope(storageId, zoneId)
                )
                .fetch();

        return candidates.stream()
                .collect(Collectors.groupingBy(
                        preference -> preference.getOrganizationMember().getId()
                ))
                .values()
                .stream()
                .map(preferences -> preferences.stream()
                        .max(Comparator.comparingInt(this::scopePriority))
                        .orElseThrow()
                )
                .filter(NotificationScopePreference::isEnabled)
                .toList();
    }

    @Override
    public boolean existsDuplicateScope(
            Long organizationMemberId,
            Long storageId,
            Long zoneId,
            Long excludeScopePreferenceId
    ) {
        Integer result = queryFactory
                .selectOne()
                .from(notificationScopePreference)
                .where(
                        notificationScopePreference.organizationMember.id.eq(organizationMemberId),
                        storageNullSafeEq(storageId),
                        zoneNullSafeEq(zoneId),
                        excludeScopePreferenceId(excludeScopePreferenceId)
                )
                .fetchFirst();

        return result != null;
    }

    @Override
    public Optional<NotificationScopePreference> findScope(Long organizationMemberId, Long storageId, Long zoneId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(notificationScopePreference)
                .leftJoin(notificationScopePreference.storage).fetchJoin()
                .leftJoin(notificationScopePreference.zone).fetchJoin()
                .where(
                        notificationScopePreference.organizationMember.id.eq(organizationMemberId),
                        storageNullSafeEq(storageId),
                        zoneNullSafeEq(zoneId)
                )
                .fetchOne());
    }

    @Override
    public Optional<NotificationScopePreference> findEffectiveScope(Long organizationMemberId, Long storageId, Long zoneId) {
        List<NotificationScopePreference> candidates = queryFactory
                .selectFrom(notificationScopePreference)
                .leftJoin(notificationScopePreference.storage).fetchJoin()
                .leftJoin(notificationScopePreference.zone).fetchJoin()
                .where(
                        notificationScopePreference.organizationMember.id.eq(organizationMemberId),
                        matchingScope(storageId, zoneId)
                )
                .fetch();

        return candidates.stream()
                .max(Comparator.comparingInt(this::scopePriority));
    }

    private BooleanExpression matchingScope(Long storageId, Long zoneId) {
        return notificationScopePreference.storage.isNull()
                .and(notificationScopePreference.zone.isNull())
                .or(notificationScopePreference.storage.id.eq(storageId)
                        .and(notificationScopePreference.zone.isNull()))
                .or(notificationScopePreference.storage.id.eq(storageId)
                        .and(notificationScopePreference.zone.id.eq(zoneId)));
    }

    private int scopePriority(NotificationScopePreference preference) {
        if (preference.getZone() != null) {
            return 3;
        }

        if (preference.getStorage() != null) {
            return 2;
        }

        return 1;
    }

    private BooleanExpression storageNullSafeEq(Long storageId) {
        return storageId == null
                ? notificationScopePreference.storage.isNull()
                : notificationScopePreference.storage.id.eq(storageId);
    }

    private BooleanExpression zoneNullSafeEq(Long zoneId) {
        return zoneId == null
                ? notificationScopePreference.zone.isNull()
                : notificationScopePreference.zone.id.eq(zoneId);
    }

    private BooleanExpression excludeScopePreferenceId(Long excludeScopePreferenceId) {
        return excludeScopePreferenceId == null
                ? null
                : notificationScopePreference.scopePreferenceId.ne(excludeScopePreferenceId);
    }
}
