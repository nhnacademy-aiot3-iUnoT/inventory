package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.dto.request.MemberDepartmentAssignRequest;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.nhnacademy.inventory.global.client.AccountClient;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberDepartmentServiceTest {

    @Mock
    private MemberDepartmentRepository memberDepartmentRepository;
    @Mock

    private OrganizationMemberService orgMemberService;

    @Mock
    private OrganizationAccessService orgAccessService;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private MemberDepartmentService memberDepartmentService;

    private OrganizationMember member;
    private MemberDepartment memberDepartment;

    @BeforeEach
    void setUp() {
        Organization organization = TestFixtures.createOrganization("테스트 조직", "1234567890");
        ReflectionTestUtils.setField(organization, "id", 1L);
        member = TestFixtures.createOrganizationMember(organization);
        ReflectionTestUtils.setField(member, "id", 1L);

        Department department = Department.create(organization, "이비인후과", "부서 설명");
        ReflectionTestUtils.setField(department, "id", 1L);
        memberDepartment = MemberDepartment.create(member, department);
    }

    @Nested
    @DisplayName("본인 부서 목록 조회")
    class GetMyDepartments {
        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(memberDepartmentRepository.findAllWithDepartmentByMemberId(member.getId())).willReturn(List.of(memberDepartment));

            List<DepartmentListResponse> responses = memberDepartmentService.getMyDepartments();

            assertEquals(1, responses.size());
            verify(memberDepartmentRepository).findAllWithDepartmentByMemberId(member.getId());
        }

        @Test
        @DisplayName("성공 - 조회 결과 없음")
        void successEmpty() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(memberDepartmentRepository.findAllWithDepartmentByMemberId(member.getId())).willReturn(List.of());

            assertTrue(memberDepartmentService.getMyDepartments().isEmpty());
        }
    }

    @Nested
    @DisplayName("부서 소속 조직원 조회")
    class GetDepartmentMembers {
        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(1L, 1L))
                    .willReturn(java.util.Optional.of(memberDepartment.getDepartment()));
            given(memberDepartmentRepository.findAllWithMemberByDepartmentId(1L))
                    .willReturn(List.of(memberDepartment));
            given(accountClient.findByUuids(List.of(member.getAccountUuid())))
                    .willReturn(List.of(new AccountResponse(member.getAccountUuid(), "member@test.com")));

            List<?> responses = memberDepartmentService.getDepartmentMembers(1L);

            assertEquals(1, responses.size());
            verify(accountClient).findByUuids(List.of(member.getAccountUuid()));
        }

        @Test
        @DisplayName("실패 - 다른 조직의 부서")
        void notFound() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(1L, 1L))
                    .willReturn(java.util.Optional.empty());

            assertThrows(DepartmentNotFoundException.class,
                    () -> memberDepartmentService.getDepartmentMembers(1L));
        }
    }

    @Nested
    @DisplayName("조직원 부서 지정")
    class AssignMemberDepartments {
        @Test
        @DisplayName("성공 - 기존 부서를 교체")
        void success() {
            given(orgAccessService.requireOwnerOrBossOrganization())
                    .willReturn(memberDepartment.getDepartment().getOrganization());
            given(orgMemberService.getMemberById(1L, 1L)).willReturn(member);
            given(departmentRepository.findAllByIdInAndOrganizationId(List.of(1L), 1L))
                    .willReturn(List.of(memberDepartment.getDepartment()));

            memberDepartmentService.assignMemberDepartments(
                    1L, new MemberDepartmentAssignRequest(List.of(1L, 1L)));

            verify(memberDepartmentRepository).deleteByOrganizationMemberId(1L);
            verify(memberDepartmentRepository).saveAll(org.mockito.ArgumentMatchers.anyList());
        }

        @Test
        @DisplayName("성공 - 빈 목록이면 부서 전체 해제")
        void success_empty() {
            given(orgAccessService.requireOwnerOrBossOrganization())
                    .willReturn(memberDepartment.getDepartment().getOrganization());
            given(orgMemberService.getMemberById(1L, 1L)).willReturn(member);
            given(departmentRepository.findAllByIdInAndOrganizationId(List.of(), 1L))
                    .willReturn(List.of());

            memberDepartmentService.assignMemberDepartments(
                    1L, new MemberDepartmentAssignRequest(null));

            verify(memberDepartmentRepository).deleteByOrganizationMemberId(1L);
            verify(memberDepartmentRepository).saveAll(List.of());
        }

        @Test
        @DisplayName("실패 - 부서가 현재 조직에 없음")
        void notFound() {
            given(orgAccessService.requireOwnerOrBossOrganization())
                    .willReturn(memberDepartment.getDepartment().getOrganization());
            given(orgMemberService.getMemberById(1L, 1L)).willReturn(member);
            given(departmentRepository.findAllByIdInAndOrganizationId(List.of(2L), 1L))
                    .willReturn(List.of());

            assertThrows(DepartmentNotFoundException.class,
                    () -> memberDepartmentService.assignMemberDepartments(
                            1L, new MemberDepartmentAssignRequest(List.of(2L))));

            org.mockito.Mockito.verify(memberDepartmentRepository, org.mockito.Mockito.never())
                    .deleteByOrganizationMemberId(1L);
        }
    }
}
