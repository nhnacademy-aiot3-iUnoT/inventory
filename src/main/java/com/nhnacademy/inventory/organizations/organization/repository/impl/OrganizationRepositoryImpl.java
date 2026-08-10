package com.nhnacademy.inventory.organizations.organization.repository.impl;

import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgSearchRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgSearchResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.QOrgSearchResponse;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import static com.nhnacademy.inventory.organizations.organization.domain.QOrganization.organization;

@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrgSearchResponse> search(OrgSearchRequest request, Pageable pageable) {
        List<OrgSearchResponse> content = queryFactory
                .select(
                    new QOrgSearchResponse(
                        organization.id,
                        organization.businessNumber,
                        organization.name,
                        organization.status,
                        organization.createdAt
                    )
                )
                .from(organization)
                .where(
                        statusEq(request.status()),
                        nameContains(request.name())
                ).orderBy(organization.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(organization.count())
                .from(organization)
                .where(
                        statusEq(request.status()),
                        nameContains(request.name())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression statusEq(OrganizationStatus status) {
        return status == null ? null : organization.status.eq(status);
    }


    private BooleanExpression nameContains(String name) {
        return name == null || name.isBlank() ? null : organization.name.containsIgnoreCase(name);
    }
}
