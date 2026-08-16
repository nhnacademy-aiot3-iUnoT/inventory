package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSignupRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationSearchResponse;
import com.nhnacademy.inventory.organizations.invitation.event.InvitationMailSendEvent;
import com.nhnacademy.inventory.organizations.invitation.exception.*;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
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

    @Mock
    private OrganizationAccessService orgAccessService;

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

        given(invitationRepository.existsActiveInvitation(eq(organization.getId()), eq(email), any(LocalDateTime.class))).willReturn(false);
        given(invitationRepository.save(any(Invitation.class))).willReturn(invitation);

        Invitation result = invitationService.createInvitation(organization, email, true);

        assertNotNull(result);
        assertEquals(invitation, result);
        verify(invitationRepository).existsActiveInvitation(eq(organization.getId()), eq(email), any(LocalDateTime.class));
        verify(invitationRepository).save(any(Invitation.class));
        verify(applicationEventPublisher).publishEvent(any(InvitationMailSendEvent.class));
    }

    @Test
    @DisplayName("초대 생성 실패 - 활성 초대 중복")
    void createInvitation_duplicate() {
        String email = "test@test.com";

        given(invitationRepository.existsActiveInvitation(eq(organization.getId()), eq(email), any(LocalDateTime.class))).willReturn(true);

        assertThrows(InvitationAlreadyExistsException.class, () -> invitationService.createInvitation(organization, email, true));

        verify(invitationRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
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
        @DisplayName("성공 - Admin 초대면 Boss로 생성")
        void signupAsOwner() {
            UUID token = invitation.getToken();
            UUID accountUuid = UUID.randomUUID();

            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));
            given(invitationRepository.findByToken(token)).willReturn(Optional.of(invitation));

            InvitationSignupRequest request = new InvitationSignupRequest(token, "test@test.com", accountUuid);

            invitationService.signupWithInvitation(request);

            verify(organizationMemberService).createUser(organization, accountUuid, OrganizationRole.ORG_BOSS);
            assertEquals(InvitationStatus.USED, invitation.getInvitationStatus());
            assertNotNull(invitation.getUsedAt());
        }

        @Test
        @DisplayName("성공 - Boss/Owner 초대면 Member로 생성")
        void signupAsMember() {
            Invitation memberInvitation = TestFixtures.createInvitationMember(organization, "test@test.com");
            UUID token = memberInvitation.getToken();
            UUID accountUuid = UUID.randomUUID();

            ReflectionTestUtils.setField(memberInvitation, "expiredAt", LocalDateTime.now().plusDays(1));
            given(invitationRepository.findByToken(token)).willReturn(Optional.of(memberInvitation));

            InvitationSignupRequest request = new InvitationSignupRequest(token, "test@test.com", accountUuid);

            invitationService.signupWithInvitation(request);

            verify(organizationMemberService).createUser(organization, accountUuid, OrganizationRole.ORG_MEMBER);
            verify(organizationMemberService, never()).createUser(any(), any(), eq(OrganizationRole.ORG_OWNER));
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

            InvitationSignupRequest request = new InvitationSignupRequest(token, "different@test.com", accountUuid);

            assertThrows(InvitationEmailMismatchException.class, () -> invitationService.signupWithInvitation(request));

            verify(organizationMemberService, never()).createUser(any(), any(), eq(OrganizationRole.ORG_BOSS));
            verify(organizationMemberService, never()).createUser(any(), any(), eq(OrganizationRole.ORG_MEMBER));
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

            InvitationSignupRequest request = new InvitationSignupRequest(token, "test@test.com", accountUuid);

            assertThrows(InvitationExpiredException.class, () -> invitationService.signupWithInvitation(request));

            verify(organizationMemberService, never()).createUser(any(), any(), eq(OrganizationRole.ORG_BOSS));
            verify(organizationMemberService, never()).createUser(any(), any(), eq(OrganizationRole.ORG_MEMBER));
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
            given(orgAccessService.requireOwnerOrBoss()).willReturn(owner);

            InvitationSearchRequest request = new InvitationSearchRequest(null, null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<InvitationSearchResponse> expected = new PageImpl<>(List.of(), pageable, 0);

            given(invitationRepository.search(organization.getId(), request, pageable)).willReturn(expected);

            Page<InvitationSearchResponse> result = invitationService.getInvitations(request, pageable);

            assertEquals(expected, result);
            verify(orgAccessService).requireOwnerOrBoss();
            verify(invitationRepository).search(organization.getId(), request, pageable);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() {
            willThrow(new ForbiddenException()).given(orgAccessService).requireOwnerOrBoss();

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

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(orgAccessService.requireOwnerOrBossOf(organization.getId())).willReturn(owner);

            invitationService.resendInvitation(invitationId);

            verify(orgAccessService).requireOwnerOrBossOf(organization.getId());
            verify(applicationEventPublisher).publishEvent(any(InvitationMailSendEvent.class));
        }

        @Test
        @DisplayName("실패 - Owner/Boss가 아님")
        void forbiddenWhenMember() {
            Long invitationId = 1L;

            ReflectionTestUtils.setField(invitation, "id", invitationId);
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(orgAccessService.requireOwnerOrBossOf(organization.getId())).willThrow(new ForbiddenException());

            assertThrows(ForbiddenException.class, () -> invitationService.resendInvitation(invitationId));

            verify(applicationEventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("실패 - 다른 조직의 초대")
        void forbiddenForAnotherOrganization() {
            Long invitationId = 1L;

            Organization anotherOrganization = TestFixtures.createOrganization("다른 조직", "9876543210");
            ReflectionTestUtils.setField(anotherOrganization, "id", 2L);

            Invitation anotherInvitation = TestFixtures.createInvitationMember(anotherOrganization, "other@test.com");
            ReflectionTestUtils.setField(anotherInvitation, "id", invitationId);
            ReflectionTestUtils.setField(anotherInvitation, "expiredAt", LocalDateTime.now().plusDays(1));

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(anotherInvitation));
            given(orgAccessService.requireOwnerOrBossOf(anotherOrganization.getId())).willThrow(new ForbiddenException());

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

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(orgAccessService.requireOwnerOrBossOf(organization.getId())).willReturn(owner);

            invitationService.cancelInvitation(invitationId);

            assertEquals(InvitationStatus.CANCELED, invitation.getInvitationStatus());
            verify(orgAccessService).requireOwnerOrBossOf(organization.getId());
        }

        @Test
        @DisplayName("실패 - 이미 사용된 초대")
        void failWhenUsed() {
            Long invitationId = 1L;

            invitation.use();
            ReflectionTestUtils.setField(invitation, "id", invitationId);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(orgAccessService.requireOwnerOrBossOf(organization.getId())).willReturn(owner);

            assertThrows(InvalidInvitationException.class, () -> invitationService.cancelInvitation(invitationId));
        }
    }

    @Nested
    @DisplayName("Owner 초대 재발급")
    class ReissueInvitationTest {

        @Test
        @DisplayName("성공")
        void successAndPreserveType() {
            Long invitationId = 1L;

            Invitation oldInvitation = TestFixtures.createInvitationMember(organization, "member@test.com");
            Invitation newInvitation = TestFixtures.createInvitationMember(organization, "member@test.com");

            ReflectionTestUtils.setField(oldInvitation, "id", invitationId);
            ReflectionTestUtils.setField(oldInvitation, "expiredAt", LocalDateTime.now().plusDays(1));
            ReflectionTestUtils.setField(newInvitation, "id", 2L);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(oldInvitation));
            given(orgAccessService.requireOwnerOrBossOf(organization.getId())).willReturn(owner);
            given(invitationRepository.existsActiveInvitation(eq(organization.getId()), eq("member@test.com"), any(LocalDateTime.class))).willReturn(false);
            given(invitationRepository.save(any(Invitation.class))).willReturn(newInvitation);

            invitationService.reissueInvitation(invitationId);

            assertEquals(InvitationStatus.REISSUED, oldInvitation.getInvitationStatus());
            verify(invitationRepository).save(any(Invitation.class));
            verify(applicationEventPublisher).publishEvent(any(InvitationMailSendEvent.class));
        }

        @Test
        @DisplayName("실패 - 사용된 초대")
        void failWhenUsed() {
            Long invitationId = 1L;

            invitation.use();
            ReflectionTestUtils.setField(invitation, "id", invitationId);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(orgAccessService.requireOwnerOrBossOf(organization.getId())).willReturn(owner);

            assertThrows(InvalidInvitationException.class, () -> invitationService.reissueInvitation(invitationId));

            verify(invitationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Admin 초대 관리")
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

            verify(invitationRepository).findById(invitationId);
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
            verify(invitationRepository).findById(invitationId);
        }

        @Test
        @DisplayName("실패 - 다른 조직의 초대")
        void failForAnotherOrganization() {
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", 2L);
            ReflectionTestUtils.setField(invitation, "id", invitationId);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            assertThrows(ForbiddenException.class, () -> invitationService.resendInvitationForAdmin(1L, invitationId));

            verify(applicationEventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("실패 - MEMBER 초대")
        void failForMemberInvitation() {
            Long organizationId = 1L;
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", organizationId);

            Invitation memberInvitation = TestFixtures.createInvitationMember(organization, "member@test.com");
            ReflectionTestUtils.setField(memberInvitation, "id", invitationId);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(memberInvitation));

            assertThrows(ForbiddenException.class, () -> invitationService.resendInvitationForAdmin(organizationId, invitationId));

            verify(applicationEventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 초대")
        void failWhenInvitationNotFound() {
            Long invitationId = 10L;

            given(invitationRepository.findById(invitationId)).willReturn(Optional.empty());

            assertThrows(InvitationNotFoundException.class, () -> invitationService.resendInvitationForAdmin(1L, invitationId));
        }
    }

    @Nested
    @DisplayName("Admin 초대 재발급")
    class AdminReissueInvitationTest {

        @Test
        @DisplayName("성공 - OWNER 초대 재발급")
        void success() {
            Long organizationId = 1L;
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", organizationId);
            ReflectionTestUtils.setField(invitation, "id", invitationId);
            ReflectionTestUtils.setField(invitation, "expiredAt", LocalDateTime.now().plusDays(1));

            Invitation newInvitation = TestFixtures.createInvitationOwner(organization, invitation.getEmail());

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
            given(invitationRepository.existsActiveInvitation(eq(organizationId), eq(invitation.getEmail()), any(LocalDateTime.class))).willReturn(false);
            given(invitationRepository.save(any(Invitation.class))).willReturn(newInvitation);

            invitationService.reissueInvitationForAdmin(organizationId, invitationId);

            assertEquals(InvitationStatus.REISSUED, invitation.getInvitationStatus());
            verify(invitationRepository).save(any(Invitation.class));
            verify(applicationEventPublisher).publishEvent(any(InvitationMailSendEvent.class));
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

        @Test
        @DisplayName("실패 - 다른 조직의 초대")
        void reissueFailForAnotherOrganization() {
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", 2L);
            ReflectionTestUtils.setField(invitation, "id", invitationId);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

            assertThrows(ForbiddenException.class, () -> invitationService.reissueInvitationForAdmin(1L, invitationId));

            verify(invitationRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - MEMBER 초대는 재발급 불가")
        void reissueFailForMemberInvitation() {
            Long organizationId = 1L;
            Long invitationId = 10L;

            ReflectionTestUtils.setField(organization, "id", organizationId);

            Invitation memberInvitation = TestFixtures.createInvitationMember(organization, "member@test.com");
            ReflectionTestUtils.setField(memberInvitation, "id", invitationId);

            given(invitationRepository.findById(invitationId)).willReturn(Optional.of(memberInvitation));

            assertThrows(ForbiddenException.class, () -> invitationService.reissueInvitationForAdmin(organizationId, invitationId));

            verify(invitationRepository, never()).save(any());
        }
    }
}
