package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationType;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSignupRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationCreateResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationSearchResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationSignupResponse;
import com.nhnacademy.inventory.organizations.invitation.event.InvitationMailSendEvent;
import com.nhnacademy.inventory.organizations.invitation.exception.InvalidInvitationException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationEmailMismatchException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationExpiredException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationNotFoundException;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.*;
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
import static org.mockito.Mockito.never;
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
        ReflectionTestUtils.setField(organization, "id", 1L);
        invitation = TestFixtures.createInvitationOwner(organization, "test@test.com");

        owner = TestFixtures.createOrganizationOwner(organization);
        member = TestFixtures.createOrganizationMember(organization);
    }

    @Test
    @DisplayName("초대 생성 성공")
    void createInvitation_success() {
        String email = "test@test.com";

        given(invitationRepository.save(any(Invitation.class))).willReturn(invitation);

        Invitation result = invitationService.createInvitation(organization, email, InvitationType.OWNER);

        assertEquals(invitation, result);
        assertEquals(InvitationType.OWNER, result.getInvitationType());

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

            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            assertDoesNotThrow(() -> invitationService.validateInvitationToken(token));
            verify(invitationRepository).findByToken(token);
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
    @DisplayName("회원가입 초대 검증 및 후처리")
    class SignupWithInvitationTest {

        @Test
        @DisplayName("성공 - OWNER 초대면 Owner로 생성")
        void signupAsOwner() {
            UUID token = invitation.getToken();
            UUID accountUuid = UUID.randomUUID();

            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            InvitationSignupRequest request = new InvitationSignupRequest(token, "test@test.com", accountUuid);

            InvitationSignupResponse response = invitationService.signupWithInvitation(request);

            assertTrue(response.isOwner());

            verify(organizationMemberService).createOwner(organization, accountUuid);

            assertEquals(InvitationStatus.USED, invitation.getInvitationStatus());

            assertNotNull(invitation.getUsedAt());
        }

        @Test
        @DisplayName("성공 - MEMBER 초대면 Member로 생성")
        void signupAsMember() {
            Invitation memberInvitation = TestFixtures.createInvitationMember(organization, "test@test.com");
            UUID token = memberInvitation.getToken();
            UUID accountUuid = UUID.randomUUID();

            ReflectionTestUtils.setField(memberInvitation, "expiredAt", LocalDateTime.now().plusDays(1));

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(memberInvitation));

            InvitationSignupRequest request = new InvitationSignupRequest(
                    token,
                    "test@test.com",
                    accountUuid
            );

            InvitationSignupResponse response = invitationService.signupWithInvitation(request);

            assertFalse(response.isOwner());

            verify(organizationMemberService).createMember(organization, accountUuid);
            verify(organizationMemberService, never()).createOwner(any(), any());

            assertEquals(InvitationStatus.USED, memberInvitation.getInvitationStatus());

            assertNotNull(memberInvitation.getUsedAt());
        }

        @Test
        @DisplayName("실패 - 이메일 불일치")
        void emailMismatch() {
            UUID token = invitation.getToken();
            UUID accountUuid = UUID.randomUUID();

            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            InvitationSignupRequest request = new InvitationSignupRequest(
                    token,
                    "different@test.com",
                    accountUuid
            );

            assertThrows(InvitationEmailMismatchException.class, () -> invitationService.signupWithInvitation(request));

            verify(organizationMemberService, never()).createOwner(any(), any());
            verify(organizationMemberService, never()).createMember(any(), any());

            assertEquals(InvitationStatus.ACTIVE, invitation.getInvitationStatus());

            assertNull(invitation.getUsedAt());
        }

        @Test
        @DisplayName("실패 - 토큰 만료")
        void expiredToken() {
            UUID token = invitation.getToken();
            UUID accountUuid = UUID.randomUUID();

            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().minusDays(1));

            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            InvitationSignupRequest request = new InvitationSignupRequest(
                    token,
                    "test@test.com",
                    accountUuid
            );

            assertThrows(InvitationExpiredException.class, () -> invitationService.signupWithInvitation(request));

            verify(organizationMemberService, never()).createOwner(any(), any());
            verify(organizationMemberService, never()).createMember(any(), any());

            assertEquals(InvitationStatus.ACTIVE, invitation.getInvitationStatus());

            assertNull(invitation.getUsedAt());
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
    @Nested
    @DisplayName("Owner 초대 재전송")
    class ResendInvitationTest {

        @Test
        @DisplayName("성공 - 자기 조직의 활성 초대")
        void success() {
            Long invitationId = 1L;

            ReflectionTestUtils.setField(invitation, "id", invitationId);
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            UserContext.setUserUuid(owner.getAccountUuid());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            given(organizationMemberService.getCurrentOrganizationMember(owner.getAccountUuid())).willReturn(owner);

            invitationService.resendInvitation(invitationId);

            verify(applicationEventPublisher).publishEvent(any(InvitationMailSendEvent.class));
        }

        @Test
        @DisplayName("실패 - Owner가 아님")
        void forbiddenWhenMember() {
            Long invitationId = 1L;

            ReflectionTestUtils.setField(invitation, "id", invitationId);
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            UserContext.setUserUuid(member.getAccountUuid());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            given(organizationMemberService.getCurrentOrganizationMember(member.getAccountUuid())).willReturn(member);

            assertThrows(ForbiddenException.class, () -> invitationService.resendInvitation(invitationId));

            verify(applicationEventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("실패 - 다른 조직의 초대")
        void forbiddenForAnotherOrganization() {
            Long invitationId = 1L;

            Organization anotherOrganization = TestFixtures.createOrganization("다른 조직", "9876543210");

            ReflectionTestUtils.setField(organization, "id", 1L);
            ReflectionTestUtils.setField(anotherOrganization, "id", 2L);

            Invitation anotherInvitation = TestFixtures.createInvitationMember(anotherOrganization, "other@test.com");

            ReflectionTestUtils.setField(anotherInvitation, "id", invitationId);
            ReflectionTestUtils.setField(anotherInvitation, "expiredAt", LocalDateTime.now().plusDays(1));

            UserContext.setUserUuid(owner.getAccountUuid());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(anotherInvitation));

            given(organizationMemberService.getCurrentOrganizationMember(owner.getAccountUuid())).willReturn(owner);

            assertThrows(ForbiddenException.class, () -> invitationService.resendInvitation(invitationId));
            verify(applicationEventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Owner 초대 취소")
    class CancelInvitationTest {

        @Test
        @DisplayName("성공")
        void success() {
            Long invitationId = 1L;

            ReflectionTestUtils.setField(invitation, "id", invitationId);
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            UserContext.setUserUuid(owner.getAccountUuid());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            given(organizationMemberService.getCurrentOrganizationMember(owner.getAccountUuid())).willReturn(owner);

            invitationService.cancelInvitation(invitationId);

            assertEquals(InvitationStatus.CANCELED, invitation.getInvitationStatus());
        }

        @Test
        @DisplayName("실패 - 이미 사용된 초대")
        void failWhenUsed() {
            Long invitationId = 1L;

            invitation.use();

            ReflectionTestUtils.setField(invitation, "id", invitationId);

            UserContext.setUserUuid(owner.getAccountUuid());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            given(organizationMemberService.getCurrentOrganizationMember(owner.getAccountUuid())).willReturn(owner);

            assertThrows(InvalidInvitationException.class, () -> invitationService.cancelInvitation(invitationId));
        }
    }

    @Nested
    @DisplayName("Owner 초대 재발급")
    class ReissueInvitationTest {

        @Test
        @DisplayName("성공 - MEMBER 타입 유지")
        void successAndPreserveType() {
            Long invitationId = 1L;

            Invitation oldInvitation = TestFixtures.createInvitationMember(organization, "member@test.com");
            Invitation newInvitation = TestFixtures.createInvitationMember(organization, "member@test.com");

            ReflectionTestUtils.setField(oldInvitation, "id", invitationId);
            ReflectionTestUtils.setField(oldInvitation, "expiredAt", LocalDateTime.now().plusDays(1));
            ReflectionTestUtils.setField(newInvitation, "id", 2L);

            UserContext.setUserUuid(owner.getAccountUuid());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(oldInvitation));

            given(organizationMemberService.getCurrentOrganizationMember(owner.getAccountUuid())).willReturn(owner);
            given(invitationRepository.save(any(Invitation.class))).willReturn(newInvitation);

            InvitationCreateResponse response = invitationService.reissueInvitation(invitationId);

            assertEquals(InvitationStatus.CANCELED, oldInvitation.getInvitationStatus());

            assertEquals(2L, response.id());
            assertEquals("member@test.com", response.email());
            assertEquals(InvitationType.MEMBER, newInvitation.getInvitationType());

            verify(applicationEventPublisher).publishEvent(any(InvitationMailSendEvent.class));
        }

        @Test
        @DisplayName("실패 - 취소된 초대는 재발급 불가")
        void failWhenCanceled() {
            Long invitationId = 1L;

            invitation.cancel();

            ReflectionTestUtils.setField(invitation, "id", invitationId);

            UserContext.setUserUuid(owner.getAccountUuid());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(organizationMemberService.getCurrentOrganizationMember(owner.getAccountUuid())).willReturn(owner);

            assertThrows(InvalidInvitationException.class,
                    () -> invitationService.reissueInvitation(invitationId)
            );

            verify(invitationRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 사용된 초대")
        void failWhenUsed() {
            Long invitationId = 1L;

            invitation.use();

            ReflectionTestUtils.setField(invitation, "id", invitationId);

            UserContext.setUserUuid(owner.getAccountUuid());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(organizationMemberService.getCurrentOrganizationMember(owner.getAccountUuid())).willReturn(owner);

            assertThrows(InvalidInvitationException.class, () -> invitationService.reissueInvitation(invitationId));

            verify(invitationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Admin 초대 재전송 및 취소")
    class AdminInvitationManagementTest {

        @Test
        @DisplayName("재전송 성공 - 같은 조직의 OWNER 초대")
        void resendSuccess() {
            Long organizationId = 1L;
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", organizationId);
            ReflectionTestUtils.setField(invitation, "id", invitationId);
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            invitationService.resendInvitationForAdmin(organizationId, invitationId);

            verify(applicationEventPublisher).publishEvent(any(InvitationMailSendEvent.class));
        }

        @Test
        @DisplayName("취소 성공 - 같은 조직의 OWNER 초대")
        void cancelSuccess() {
            Long organizationId = 1L;
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", organizationId);
            ReflectionTestUtils.setField(invitation, "id", invitationId);
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            invitationService.cancelInvitationForAdmin(organizationId, invitationId);

            assertEquals(InvitationStatus.CANCELED, invitation.getInvitationStatus());
        }

        @Test
        @DisplayName("실패 - 다른 조직의 초대")
        void failForAnotherOrganization() {
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", 2L);
            ReflectionTestUtils.setField(invitation, "id", invitationId);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            assertThrows(ForbiddenException.class, () -> invitationService.resendInvitationForAdmin(1L, invitationId));
        }

        @Test
        @DisplayName("실패 - MEMBER 타입 초대")
        void failForMemberInvitation() {
            Long organizationId = 1L;
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", organizationId);

            Invitation memberInvitation = TestFixtures.createInvitationMember(organization, "member@test.com");
            ReflectionTestUtils.setField(memberInvitation, "id", invitationId);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(memberInvitation));

            assertThrows(ForbiddenException.class, () -> invitationService.resendInvitationForAdmin(organizationId, invitationId));
        }
    }

    @Nested
    @DisplayName("Admin 초대 재발급")
    class AdminReissueInvitationTest {

        @Test
        @DisplayName("성공 - OWNER 타입 유지")
        void success() {
            Long organizationId = 1L;
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", organizationId);
            ReflectionTestUtils.setField(invitation, "id", invitationId);
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            Invitation newInvitation = TestFixtures.createInvitationOwner(organization, invitation.getEmail());

            ReflectionTestUtils.setField(newInvitation, "id", 11L);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(invitationRepository.save(any(Invitation.class))).willReturn(newInvitation);

            invitationService.reissueInvitationForAdmin(organizationId, invitationId);

            assertEquals(InvitationStatus.CANCELED, invitation.getInvitationStatus());
            assertEquals(InvitationType.OWNER, newInvitation.getInvitationType());
        }

        @Test
        @DisplayName("실패 - 사용된 초대")
        void failWhenUsed() {
            Long organizationId = 1L;
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", organizationId);
            ReflectionTestUtils.setField(invitation, "id", invitationId);

            invitation.use();

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            assertThrows(InvalidInvitationException.class, () -> invitationService.reissueInvitationForAdmin(organizationId, invitationId));

            verify(invitationRepository, never()).save(any());
        }
    }
}
