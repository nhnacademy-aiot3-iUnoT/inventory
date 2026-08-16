package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.exception.MemberDepartmentAlreadyExistsException;
import com.nhnacademy.inventory.organizations.department.exception.MemberDepartmentNotInException;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private MemberDepartmentService memberDepartmentService;

    private Organization organization;
    private OrganizationMember member;
    private Department department;
    private MemberDepartment memberDepartment;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "1234567890");
        ReflectionTestUtils.setField(organization, "id", 1L);

        member = TestFixtures.createOrganizationMember(organization);

        department = Department.create(organization, "이비인후과", "부서 설명");
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
            given(memberDepartmentRepository.findAllByOrganizationMemberId(member.getId())).willReturn(List.of(memberDepartment));

            List<DepartmentListResponse> responses = memberDepartmentService.getMyDepartments();

            assertEquals(1, responses.size());

            verify(orgAccessService).getCurrentMember();
            verify(memberDepartmentRepository).findAllByOrganizationMemberId(member.getId());
        }

        @Test
        @DisplayName("성공 - 조회 결과 없음")
        void success_empty() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(memberDepartmentRepository.findAllByOrganizationMemberId(member.getId())).willReturn(List.of());

            List<DepartmentListResponse> responses = memberDepartmentService.getMyDepartments();

            assertTrue(responses.isEmpty());

            verify(orgAccessService).getCurrentMember();
            verify(memberDepartmentRepository).findAllByOrganizationMemberId(member.getId());
        }
    }

    @Nested
    @DisplayName("조직원 부서 할당")
    class AssignDepartment {

        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(memberDepartmentRepository.existsByOrganizationMemberIdAndDepartmentId(member.getId(), department.getId())).willReturn(false);
            given(orgMemberService.getMemberById(member.getId(), organization.getId())).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(department.getId(), organization.getId())).willReturn(Optional.of(department));

            memberDepartmentService.assignDepartment(member.getId(), department.getId());

            verify(orgAccessService).requireOwnerOrBossOrganization();
            verify(orgMemberService).getMemberById(member.getId(), organization.getId());
            verify(departmentRepository).findByIdAndOrganizationId(department.getId(), organization.getId());
            verify(memberDepartmentRepository).save(any(MemberDepartment.class));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willThrow(new ForbiddenException());

            assertThrows(ForbiddenException.class, () -> memberDepartmentService.assignDepartment(member.getId(), department.getId()));

            verify(memberDepartmentRepository, never()).existsByOrganizationMemberIdAndDepartmentId(anyLong(), anyLong());
            verify(orgMemberService, never()).getMemberById(anyLong(), anyLong());
            verify(departmentRepository, never()).findByIdAndOrganizationId(anyLong(), anyLong());
            verify(memberDepartmentRepository, never()).save(any(MemberDepartment.class));
        }

        @Test
        @DisplayName("실패 - 이미 부서 지정됨")
        void already_exists() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(memberDepartmentRepository.existsByOrganizationMemberIdAndDepartmentId(member.getId(), department.getId())).willReturn(true);

            assertThrows(MemberDepartmentAlreadyExistsException.class, () -> memberDepartmentService.assignDepartment(member.getId(), department.getId()));

            verify(orgMemberService, never()).getMemberById(anyLong(), anyLong());
            verify(departmentRepository, never()).findByIdAndOrganizationId(anyLong(), anyLong());
            verify(memberDepartmentRepository, never()).save(any(MemberDepartment.class));
        }

        @Test
        @DisplayName("실패 - 조직원 없음")
        void member_not_found() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(memberDepartmentRepository.existsByOrganizationMemberIdAndDepartmentId(member.getId(), department.getId())).willReturn(false);
            given(orgMemberService.getMemberById(member.getId(), organization.getId())).willThrow(new RuntimeException());

            assertThrows(RuntimeException.class, () -> memberDepartmentService.assignDepartment(member.getId(), department.getId()));

            verify(departmentRepository, never()).findByIdAndOrganizationId(anyLong(), anyLong());
            verify(memberDepartmentRepository, never()).save(any(MemberDepartment.class));
        }

        @Test
        @DisplayName("실패 - 부서 없음")
        void department_not_found() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(memberDepartmentRepository.existsByOrganizationMemberIdAndDepartmentId(member.getId(), department.getId())).willReturn(false);
            given(orgMemberService.getMemberById(member.getId(), organization.getId())).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(department.getId(), organization.getId())).willReturn(Optional.empty());

            assertThrows(DepartmentNotFoundException.class, () -> memberDepartmentService.assignDepartment(member.getId(), department.getId()));

            verify(memberDepartmentRepository, never()).save(any(MemberDepartment.class));
        }
    }

    @Nested
    @DisplayName("조직원 부서 지정 해제")
    class RemoveDepartment {

        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(orgMemberService.getMemberById(member.getId(), organization.getId())).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(department.getId(), organization.getId())).willReturn(Optional.of(department));
            given(memberDepartmentRepository.findByDepartmentIdAndOrganizationMemberId(department.getId(), member.getId())).willReturn(Optional.of(memberDepartment));

            memberDepartmentService.removeDepartment(member.getId(), department.getId());

            verify(orgAccessService).requireOwnerOrBossOrganization();
            verify(orgMemberService).getMemberById(member.getId(), organization.getId());
            verify(departmentRepository).findByIdAndOrganizationId(department.getId(), organization.getId());
            verify(memberDepartmentRepository).findByDepartmentIdAndOrganizationMemberId(department.getId(), member.getId());
            verify(memberDepartmentRepository).delete(memberDepartment);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willThrow(new ForbiddenException());

            assertThrows(ForbiddenException.class, () -> memberDepartmentService.removeDepartment(member.getId(), department.getId()));

            verify(orgMemberService, never()).getMemberById(anyLong(), anyLong());
            verify(departmentRepository, never()).findByIdAndOrganizationId(anyLong(), anyLong());
            verify(memberDepartmentRepository, never()).findByDepartmentIdAndOrganizationMemberId(anyLong(), anyLong());
            verify(memberDepartmentRepository, never()).delete(any(MemberDepartment.class));
        }

        @Test
        @DisplayName("실패 - 부서 없음")
        void department_not_found() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(orgMemberService.getMemberById(member.getId(), organization.getId())).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(department.getId(), organization.getId())).willReturn(Optional.empty());

            assertThrows(DepartmentNotFoundException.class, () -> memberDepartmentService.removeDepartment(member.getId(), department.getId()));

            verify(memberDepartmentRepository, never()).findByDepartmentIdAndOrganizationMemberId(anyLong(), anyLong());
            verify(memberDepartmentRepository, never()).delete(any(MemberDepartment.class));
        }

        @Test
        @DisplayName("실패 - 부서 지정되어 있지 않음")
        void not_in() {
            given(orgAccessService.requireOwnerOrBossOrganization()).willReturn(organization);
            given(orgMemberService.getMemberById(member.getId(), organization.getId())).willReturn(member);
            given(departmentRepository.findByIdAndOrganizationId(department.getId(), organization.getId())).willReturn(Optional.of(department));
            given(memberDepartmentRepository.findByDepartmentIdAndOrganizationMemberId(department.getId(), member.getId())).willReturn(Optional.empty());

            assertThrows(MemberDepartmentNotInException.class, () -> memberDepartmentService.removeDepartment(member.getId(), department.getId()));

            verify(memberDepartmentRepository, never()).delete(any(MemberDepartment.class));
        }
    }
}
