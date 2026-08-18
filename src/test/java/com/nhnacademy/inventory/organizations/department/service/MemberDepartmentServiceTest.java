package com.nhnacademy.inventory.organizations.department.service;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.repository.DepartmentRepository;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @InjectMocks
    private MemberDepartmentService memberDepartmentService;

    private OrganizationMember member;
    private MemberDepartment memberDepartment;

    @BeforeEach
    void setUp() {
        Organization organization = TestFixtures.createOrganization("테스트 조직", "1234567890");
        ReflectionTestUtils.setField(organization, "id", 1L);
        member = TestFixtures.createOrganizationMember(organization);

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
            given(memberDepartmentRepository.findAllByOrganizationMemberId(member.getId())).willReturn(List.of(memberDepartment));

            List<DepartmentListResponse> responses = memberDepartmentService.getMyDepartments();

            assertEquals(1, responses.size());
            verify(memberDepartmentRepository).findAllByOrganizationMemberId(member.getId());
        }

        @Test
        @DisplayName("성공 - 조회 결과 없음")
        void successEmpty() {
            given(orgAccessService.getCurrentMember()).willReturn(member);
            given(memberDepartmentRepository.findAllByOrganizationMemberId(member.getId())).willReturn(List.of());

            assertTrue(memberDepartmentService.getMyDepartments().isEmpty());
        }
    }
}
