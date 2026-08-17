package com.nhnacademy.inventory.reports.report.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrganizationMemberValidatorTest {

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private OrganizationMemberValidator organizationMemberValidator;

    @Test
    @DisplayName("존재하지 않는 조직 멤버라면 ForbiddenException이 발생한다.")
    void validateAndGet_WhenOrganizationMemberNotExists_ThrowsException() {
        // given
        UUID accountUuid = UUID.randomUUID();

        given(organizationMemberRepository.findByAccountUuid(accountUuid))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> organizationMemberValidator.validateAndGet(accountUuid))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("조직 멤버가 존재하면 해당 멤버를 반환한다.")
    void validateAndGet_WhenOrganizationMemberExists_ReturnsMember() {
        // given
        UUID accountUuid = UUID.randomUUID();
        OrganizationMember member = mock(OrganizationMember.class);

        given(organizationMemberRepository.findByAccountUuid(accountUuid))
                .willReturn(Optional.of(member));

        // when
        OrganizationMember result = organizationMemberValidator.validateAndGet(accountUuid);

        // then
        assertThat(result)
                .isEqualTo(member);
    }
}