package com.nhnacademy.inventory.organizations.member.repository.impl;

import com.nhnacademy.inventory.organizations.department.domain.QMemberDepartment;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static com.nhnacademy.inventory.organizations.member.domain.QOrganizationMember.organizationMember;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class OrganizationMemberRepositoryImpl implements OrganizationMemberCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrganizationMember> findMembers(Long organizationId, List<UUID> accountUuids, OrganizationRole role, boolean hasDepartment, Pageable pageable) {
        List<OrganizationMember> content = queryFactory
                .selectFrom(organizationMember)
                .where(organizationMember.organization.id.eq(organizationId),
                        accountUuidIn(accountUuids),
                        roleEq(role),
                        departmentCondition(hasDepartment)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(organizationMember.count())
                .from(organizationMember)
                .where(
                        organizationMember.organization.id.eq(organizationId),
                        accountUuidIn(accountUuids),
                        roleEq(role),
                        departmentCondition(hasDepartment)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression accountUuidIn(List<UUID> accountUuids) {
        return accountUuids == null ? null : organizationMember.accountUuid.in(accountUuids);
    }

    private BooleanExpression roleEq(OrganizationRole role) {
        return role == null ? null : organizationMember.organizationRole.eq(role);
    }

    private BooleanExpression departmentCondition(boolean hasDepartment) {
        QMemberDepartment memberDepartment = QMemberDepartment.memberDepartment;

        BooleanExpression exits =
                JPAExpressions
                        .selectOne()
                        .from(memberDepartment)
                        .where(memberDepartment.organizationMember.id.eq(organizationMember.id))
                        .exists();

        return hasDepartment ? exits : exits.not();
    }
}
