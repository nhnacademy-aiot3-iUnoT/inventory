package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationSearchResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationVerifyResponse;
import com.nhnacademy.inventory.organizations.invitation.event.InvitationMailSendEvent;
import com.nhnacademy.inventory.organizations.invitation.exception.InvalidInvitationException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationEmailMismatchException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationNotFoundException;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private OrganizationMemberService organizationMemberService;

    @InjectMocks
    private InvitationService invitationService;

    private Organization organization;
    private Invitation invitation;
    private OrganizationMember owner;
    private OrganizationMember member;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "1234567890");
        invitation = TestFixtures.createInvitation(organization, "test@test.com");

        owner = TestFixtures.createOrganizationMember(organization);
        owner.changeRole(OrganizationRole.ORG_OWNER);

        member = TestFixtures.createOrganizationMember(organization);
        member.changeRole(OrganizationRole.ORG_MEMBER);
    }

    @Test
    @DisplayName("초대 생성 성공")
    void createInvitation_success() {
        String email = "test@test.com";

        given(invitationRepository.save(any(Invitation.class))).willReturn(invitation);

        Invitation result = invitationService.createInvitation(organization, email);

        assertEquals(invitation, result);

        verify(invitationRepository).save(any(Invitation.class));
        verify(applicationEventPublisher).publishEvent(any(InvitationMailSendEvent.class));
    }

    @Nested
    @DisplayName("초대 토큰 검증")
    class ValidateInvitationTest {

        @Test
        @DisplayName("성공")
        void success() {
            UUID token = invitation.getToken();

            Invitation invitation = TestFixtures.createInvitation(organization, "test@test.com");
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(2));

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            InvitationVerifyResponse response = invitationService.validateInvitationToken(token);

            assertEquals(invitation.getEmail(), response.email());
            assertEquals(organization.getName(), response.organizationName());
        }

        @Test
        @DisplayName("실패 - 초대 없음")
        void notFound() {
            UUID token = invitation.getToken();

            given(invitationRepository.findByToken(token)).willReturn(Optional.empty());

            assertThrows(InvitationNotFoundException.class, () -> invitationService.validateInvitationToken(token));
        }

        @Test
        @DisplayName("실패 - 이미 사용된 초대")
        void invalidStatus() {
            UUID token = invitation.getToken();

            invitation.use();

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            assertThrows(InvalidInvitationException.class, () -> invitationService.validateInvitationToken(token));
        }
    }

        @Nested
    @DisplayName("회원가입 초대 검증")
    class ValidateInvitationForSignupTest {
        @Test
        @DisplayName("성공")
        void success() {
            UUID token = invitation.getToken();

            Invitation invitation = TestFixtures.createInvitation(organization, "test@test.com");
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(2));

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            assertDoesNotThrow(() -> invitationService.validateInvitationForSignup(token,"test@test.com"));
        }

        @Test
        @DisplayName("실패 - 이메일 불일치")
        void emailMismatch() {
            UUID token = invitation.getToken();

            Invitation invitation = TestFixtures.createInvitation(organization, "test@test.com");
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(2));

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            assertThrows(InvitationEmailMismatchException.class,
                    () -> invitationService.validateInvitationForSignup(token, "different@test.com"));
        }
    }

    @Nested
    @DisplayName("초대 목록 조회")
    class GetInvitationListTest {
        @Test
        @DisplayName("성공")
        void success() {
            UserContext.setUserUuid(owner.getAccountUuid());
            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(owner);

            InvitationSearchRequest request = new InvitationSearchRequest(null, null);
            Pageable pageable = PageRequest.of(0, 10);

            Page<InvitationSearchResponse> expected = new PageImpl<>(List.of(), pageable, 0);

            given(invitationRepository.search(organization.getId(), request, pageable)).willReturn(expected);

            Page<InvitationSearchResponse> result = invitationService.getInvitations(request, pageable);

            assertEquals(expected, result);
            verify(invitationRepository).search(organization.getId(), request, pageable);
        }

        @Test
        @DisplayName("실패 - OWNER 아님")
        void forbidden() {
            UserContext.setUserUuid(member.getAccountUuid());
            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(member);

            InvitationSearchRequest request = new InvitationSearchRequest(null, null);
            Pageable pageable = PageRequest.of(0, 10);

            assertThrows(ForbiddenException.class, () -> invitationService.getInvitations(request, pageable));
        }
    }

    @Nested
    @DisplayName("초대 사용")
    class UseInvitation {
        @Test
        @DisplayName("성공")
        void success() {
            UUID token = invitation.getToken();

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            invitationService.useInvitation(token);

            assertEquals(InvitationStatus.USED,invitation.getInvitationStatus());
        }

        @Test
        @DisplayName("실패 - 초대 없음")
        void notFound() {
            UUID token = invitation.getToken();

            given(invitationRepository.findByToken(token)).willReturn(Optional.empty());

            assertThrows(InvitationNotFoundException.class, () -> invitationService.useInvitation(token));
        }
    }

    @Nested
    @DisplayName("메일 발송 처리")
    class MarkEmailSent {
        @Test
        @DisplayName("성공")
        void success() {
            UUID token = invitation.getToken();

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            assertNull(invitation.getEmailSentAt());

            invitationService.markEmailSent(token);

            assertNotNull(invitation.getEmailSentAt());
        }

        @Test
        @DisplayName("실패 - 초대 없음")
        void notFound() {
            UUID token = invitation.getToken();

            given(invitationRepository.findByToken(token)).willReturn(Optional.empty());

            assertThrows(InvitationNotFoundException.class, () -> invitationService.markEmailSent(token));
        }
    }

}
