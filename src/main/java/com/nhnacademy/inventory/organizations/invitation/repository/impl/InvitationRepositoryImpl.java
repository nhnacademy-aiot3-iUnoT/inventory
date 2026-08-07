package com.nhnacademy.inventory.organizations.invitation.repository.impl;

import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationSearchResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.response.QInvitationSearchResponse;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import static com.nhnacademy.inventory.organizations.invitation.domain.QInvitation.invitation;


@RequiredArgsConstructor
public class InvitationRepositoryImpl implements InvitationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InvitationSearchResponse> search(Long organizationId, InvitationSearchRequest request, Pageable pageable) {
        List<InvitationSearchResponse> content = queryFactory
                .select(
                    new QInvitationSearchResponse(
                        invitation.id,
                        invitation.email,
                        invitation.invitationStatus,
                        invitation.createdAt,
                        invitation.expiredAt
                    )
                )
                .from(invitation)
                .where(
                        invitation.organization.id.eq(organizationId),
                        statusEq(request.status()),
                        emailContains(request.email())
                ).orderBy(invitation.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(invitation.count())
                .from(invitation)
                .where(
                        statusEq(request.status()),
                        emailContains(request.email())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression statusEq(InvitationStatus status) {
        return status == null ? null : invitation.invitationStatus.eq(status);
    }

    private BooleanExpression emailContains(String email) {
        return email == null || email.isBlank() ? null : invitation.email.contains(email);
    }
}
