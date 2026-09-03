package com.nhnacademy.inventory.inventories.alert.repository;

import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.dto.AlertInfoResponse;
import com.nhnacademy.inventory.inventories.alert.dto.AlertSearchCondition;
import com.nhnacademy.inventory.inventories.alert.dto.QAlertInfoResponse;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.nhnacademy.inventory.inventories.alert.domain.QAlert.alert;

@RequiredArgsConstructor
public class AlertRepositoryImpl implements AlertRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AlertInfoResponse> searchByCondition(OrganizationMember organizationMember, AlertSearchCondition condition, Pageable pageable) {

        List<AlertInfoResponse> responseList = queryFactory
                .select(new QAlertInfoResponse(
                        alert.id,
                        alert.organizationMember.organization.id,
                        alert.organizationMember.organization.name,
                        alert.alertType,
                        alert.message,
                        alert.isChecked,
                        alert.createdAt))
                .from(alert)
                .where(
                        alert.organizationMember.eq(organizationMember),
                        alertTypeEq(condition.alertType()),
                        isCheckedEq(condition.isChecked())
                )
                .orderBy(alert.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(alert.count())
                .from(alert)
                .where(
                        alert.organizationMember.eq(organizationMember),
                        alertTypeEq(condition.alertType()),
                        isCheckedEq(condition.isChecked())
                )
                .fetchOne();

        return new PageImpl<>(responseList, pageable, total != null ? total: 0L);
    }

    private BooleanExpression alertTypeEq(AlertType alertType){
        return alertType != null ? alert.alertType.eq(alertType) : null;
    }

    private BooleanExpression isCheckedEq(Boolean isChecked){
        return isChecked != null ? alert.isChecked.eq(isChecked) : null;
    }
}
