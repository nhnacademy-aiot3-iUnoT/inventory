package com.nhnacademy.inventory.enviroments.event.repository;

import com.nhnacademy.inventory.enviroments.event.domain.BreachType;
import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentType;
import com.nhnacademy.inventory.enviroments.event.dto.EnvironmentEventInfoResponse;
import com.nhnacademy.inventory.enviroments.event.dto.EnvironmentEventSearchCondition;
import com.nhnacademy.inventory.enviroments.event.dto.QEnvironmentEventInfoResponse;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.nhnacademy.inventory.enviroments.event.domain.QEnvironmentEvent.environmentEvent;
import static com.nhnacademy.inventory.organizations.organization.domain.QOrganization.organization;
import static com.nhnacademy.inventory.organizations.storage.domain.QStorage.storage;
import static com.nhnacademy.inventory.organizations.zone.domain.QZone.zone;

@Repository
@RequiredArgsConstructor
public class EnvironmentEventRepositoryImpl implements EnvironmentEventRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EnvironmentEventInfoResponse> findEnvironmentEvents(
            Zone targetZone,
            EnvironmentEventSearchCondition condition,
            Pageable pageable
    ) {
        List<EnvironmentEventInfoResponse> content = queryFactory
                .select(new QEnvironmentEventInfoResponse(
                        environmentEvent.id,
                        organization.id,
                        storage.id,
                        zone.id,
                        organization.name,
                        storage.name,
                        zone.name,
                        environmentEvent.detectedValue,
                        environmentEvent.thresholdValue,
                        environmentEvent.environmentType,
                        environmentEvent.breachType
                ))
                .from(environmentEvent)
                .join(environmentEvent.zone, zone)
                .join(zone.storage, storage)
                .join(storage.organization, organization)
                .where(
                        environmentEvent.zone.eq(targetZone),
                        environmentTypeEq(condition.environmentType()),
                        breachTypeEq(condition.breachType())
                )
                .orderBy(getOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(environmentEvent.count())
                .from(environmentEvent)
                .where(
                        environmentEvent.zone.eq(targetZone),
                        environmentTypeEq(condition.environmentType()),
                        breachTypeEq(condition.breachType())
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression environmentTypeEq(EnvironmentType environmentType){
        return environmentType != null ? environmentEvent.environmentType.eq(environmentType) : null;
    }

    private BooleanExpression breachTypeEq(BreachType breachType){
        return breachType != null ? environmentEvent.breachType.eq(breachType) : null ;
    }

    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable){
        if(!pageable.getSort().isSorted()){
            return environmentEvent.createdAt.desc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        return order.isAscending() ? environmentEvent.createdAt.asc() : environmentEvent.createdAt.desc();
    }
}
