package com.nhnacademy.inventory.reports.report.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrganizationMemberValidatorTest {

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private OrganizationMemberValidator organizationMemberValidator;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("존재하지 않는 조직 멤버라면 ForbiddenException이 발생한다.")
    void validate_WhenOrganizationMemberNotExists_ThrowsException() {
        // given
        long organizationId = 1L;
        UUID accountUuid = UUID.randomUUID();
        UserContext.setUserUuid(accountUuid);

        given(organizationMemberRepository.findByAccountUuid(accountUuid))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> organizationMemberValidator.validate(organizationId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("다른 조직에 속한 경우 ForbiddenException이 발생한다.")
    void validate_WhenOtherOrganization_ThrowsException() {
        // given
        long organizationId = 1L;
        UUID accountUuid = UUID.randomUUID();
        UserContext.setUserUuid(accountUuid);
        Organization organization = mock(Organization.class);
        OrganizationMember member = mock(OrganizationMember.class);

        given(organizationMemberRepository.findByAccountUuid(accountUuid))
                .willReturn(Optional.of(member));
        given(member.getOrganization())
                .willReturn(organization);
        given(organization.getId())
                .willReturn(2L);

        // when & then
        assertThatThrownBy(() -> organizationMemberValidator.validate(organizationId))
                .isInstanceOf(ForbiddenException.class);
    }
}